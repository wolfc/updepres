/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.up.depres.dpkg;

import org.jboss.up.depres.*;
import org.jboss.up.depres.Package;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class AvailableReader {
    private static final Logger LOG = Logger.getLogger(AvailableReader.class.getName());

    // ignore the following relationship directives: Enhances, Recommends, Suggests
    private static final Set<String> IGNORED_FIELDS = new HashSet<>(
            Arrays.asList("Architecture", "Bugs", "Built-Using", "Description-md5", "Enhances", "Essential", "Installed-Size", "Filename",
                    "Gstreamer-Encoders", "Gstreamer-Decoders", "Gstreamer-Elements", "Gstreamer-Uri-Sinks", "Gstreamer-Uri-Sources", "Gstreamer-Version", "Homepage",
                    "Maintainer", "MD5sum", "Npp-Applications", "Multi-Arch", "Npp-Description", "Npp-File", "Npp-Filename", "Npp-Mimetype", "Npp-Name", "Origin", "Orig-Maintainer", "Original-Maintainer",
                    "Priority", "Python-Version", "Python3-Version", "Recommends", "Section", "SHA1",
                    "SHA256", "Size", "Source", "Suggests", "Supported", "Tag", "Task", "Xul-Appid"));

    static class PackageVersionDeclaration {
        final String name;
        String breaks;
        String depends;
        String version;
        String provides;

        PackageVersionDeclaration(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Package " + name;
        }
    }

    private static Dependency dependency(final PackageVersion dependent, final String expr) {
        final int i = expr.indexOf('(');
        final String condition;
        final String providerName;
        if (i != -1) {
            condition = expr.substring(i + 1, expr.length() - 1);
            providerName = expr.substring(0, i - 1).trim();
        } else {
            condition = null;
            providerName = expr;
        }
        return new Dependency(dependent, dependent.getUniverse().pkg(providerName), condition);
    }

    private static Iterable<String> each(final StringTokenizer tokenizer) {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return tokenizer.hasMoreTokens();
                    }

                    @Override
                    public String next() {
                        return tokenizer.nextToken();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public static Universe load() throws IOException {
        return load("/var/lib/dpkg/available");
    }

    public static Universe load(final String fileName) throws IOException {
        final Universe universe = new Universe();
        final LineNumberReader reader = new LineNumberReader(new FileReader(fileName));
        PackageVersionDeclaration currentPkg = null;
        String line = "";
        try {
            while((line = reader.readLine()) != null) {
                //System.out.println(line);
                if (line.length() == 0) {
                    process(universe, currentPkg);
                    currentPkg = null;
                    continue;
                }
                final int colon = line.indexOf(':');
                if (colon == -1)
                    throw new IllegalStateException("Failed find ':' in line #" + reader.getLineNumber() + ": " + line);
                final String field = line.substring(0, colon);
                final String data = line.substring(colon + 1).trim();
                if (IGNORED_FIELDS.contains(field))
                    continue;
                if (field.equals("Breaks")) {
                    currentPkg.breaks = data;
                } else if (field.equals("Conflicts")) {
                    // TODO
                } else if (field.equals("Description")) {
                    // ignore
                    do {
                        reader.mark(1024);
                        final String multiline = reader.readLine();
                        if (multiline == null)
                            break;
                        if (multiline.length() == 0 || multiline.charAt(0) != ' ') {
                            reader.reset();
                            break;
                        }
                    } while(true);
                } else if (field.equals("Depends")) {
                    currentPkg.depends = data;
                } else if (field.equals("Pre-Depends")) {
                    // TODO
                } else if (field.equals("Package")) {
                    assert currentPkg == null : "already processing package " + currentPkg;
                    LOG.fine(data);
                    currentPkg = new PackageVersionDeclaration(data);
                } else if (field.equals("Provides")) {
                    currentPkg.provides = data;
                } else if (field.equals("Replaces")) {
                    // TODO
                } else if (field.equals("Version")) {
                    currentPkg.version = data;
                } else {
                    throw new IllegalStateException("Unknown field " + field);
                }
            }
            if (currentPkg != null)
                process(universe, currentPkg);
            return universe;
        } catch (IOException e) {
            throw new IOException("Failed to process line #" + reader.getLineNumber() + ": " + line, e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to process line #" + reader.getLineNumber() + ": " + line, e);
        }
    }

    public static void main(final String[] args) throws Exception {
        load();
    }

    private static void process(final Universe universe, final PackageVersionDeclaration declaration) {
        final Package pkg = universe.pkg(declaration.name);
        final PackageVersion packageVersion = pkg.addVersion(declaration.version);
        processBreaks(packageVersion, declaration.breaks);
        processDepends(packageVersion, declaration.depends);
        if (declaration.provides != null) {
            final StringTokenizer st = new StringTokenizer(declaration.provides, ",");
            for (String s : each(st)) {
                packageVersion.addProvides(s.trim());
            }
        }
    }

    private static void processBreaks(final PackageVersion currentPackageVersion, final String data) {
        if (data == null)
            return;
        final StringTokenizer st = new StringTokenizer(data, ",");
        for (String s : each(st)) {
            final int i = s.indexOf('(');
            final String condition;
            final String name;
            if (i != -1) {
                condition = s.substring(i + 1, s.length() - 1);
                name = s.substring(0, i - 1).trim();
            } else {
                condition = null;
                name = s;
            }
            currentPackageVersion.addBreaks(currentPackageVersion.getUniverse().pkg(name), condition);
        }
    }

    private static void processDepends(final PackageVersion currentPackageVersion, final String data) {
        if (data == null)
            return;
        LOG.fine("Depends: " + data);
        final StringTokenizer stAnd = new StringTokenizer(data, ",");
        for (String s : each(stAnd)) {
            s = s.trim();
            LOG.fine("  " + s);
            final List<Dependency> dependencies = new ArrayList<>();
            final StringTokenizer stOr = new StringTokenizer(s, "|");
            for (String sOr : each(stOr)) {
                sOr = sOr.trim();
                LOG.fine("    " + sOr);
                dependencies.add(dependency(currentPackageVersion, sOr));
            }
            currentPackageVersion.addDependency(dependencies);
        }
    }
}
