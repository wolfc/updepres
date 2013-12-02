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

import org.jboss.up.depres.Universe;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class AvailableReader {
    // ignore the following relationship directives: Enhances, Recommends, Suggests
    private static final Set<String> IGNORED_FIELDS = new HashSet<>(
            Arrays.asList("Architecture", "Bugs", "Built-Using", "Description-md5", "Enhances", "Essential", "Installed-Size", "Filename",
                    "Gstreamer-Encoders", "Gstreamer-Decoders", "Gstreamer-Elements", "Gstreamer-Uri-Sinks", "Gstreamer-Uri-Sources", "Gstreamer-Version", "Homepage",
                    "Maintainer", "MD5sum", "Npp-Applications", "Multi-Arch", "Npp-Description", "Npp-File", "Npp-Filename", "Npp-Mimetype", "Npp-Name", "Origin", "Orig-Maintainer", "Original-Maintainer",
                    "Priority", "Python-Version", "Python3-Version", "Recommends", "Section", "SHA1",
                    "SHA256", "Size", "Source", "Suggests", "Supported", "Tag", "Task", "Xul-Appid"));

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

    public static void main(final String[] args) throws Exception {
        final Universe universe = new Universe();
        final LineNumberReader reader = new LineNumberReader(new FileReader("/var/lib/dpkg/available"));
        org.jboss.up.depres.Package currentPkg = null;
        String line = "";
        try {
            while((line = reader.readLine()) != null) {
                //System.out.println(line);
                if (line.length() == 0) {
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
                    // TODO
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
                    // TODO
                    System.out.println("Depends: " + data);
                    final StringTokenizer stAnd = new StringTokenizer(data, ",");
                    for (String s : each(stAnd)) {
                        s = s.trim();
                        System.out.println("  " + s);
                        final StringTokenizer stOr = new StringTokenizer(s, "|");
                        for (String sOr : each(stOr)) {
                            sOr = sOr.trim();
                            System.out.println("    " + sOr);
                        }
                    }
                } else if (field.equals("Pre-Depends")) {
                    // TODO
                    // treat this a normal dependency for now
                } else if (field.equals("Package")) {
                    assert currentPkg == null : "already processing package " + currentPkg;
                    currentPkg = universe.addPackage(data);
                } else if (field.equals("Provides")) {
                    // TODO
                } else if (field.equals("Replaces")) {
                    // TODO
                } else if (field.equals("Version")) {
                    currentPkg.addVersion(data);
                } else {
                    throw new IllegalStateException("Unknown field " + field);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to process line #" + reader.getLineNumber() + ": " + line);
            throw e;
        }
    }
}
