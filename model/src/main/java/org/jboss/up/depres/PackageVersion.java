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
package org.jboss.up.depres;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class PackageVersion {
    private final Package pkg;
    private final String version;

    private Collection<BreaksDependency> breaks = new ArrayList<>();
    private List<OrDependency> dependencies = new ArrayList<>();
    private Collection<Provides> provides = new ArrayList<>();

    PackageVersion(final Package pkg, final String version) {
        this.pkg = pkg;
        this.version = version;
    }

    public void addBreaks(final Package pkg, final String condition) {
        final BreaksDependency breaksDependency = new BreaksDependency(this, pkg, condition);
        breaks.add(breaksDependency);
        pkg.breakers.add(breaksDependency);
    }

    /**
     * Add a dependency which can be satisfied by one of the listed dependencies.
     *
     * @param dependencies
     */
    public void addDependency(final List<Dependency> dependencies) {
        this.dependencies.add(new OrDependency(this, dependencies));
    }

    public void addProvides(final String provides) {
        final Package pkg = getUniverse().pkg(provides);
        if (pkg.getVersions().size() == 0) {
            pkg.addVersion("virtual");
        }
        // TODO: or do a reverse dependency?
        final Provides providesRelation = new Provides(this, pkg);
        this.provides.add(providesRelation);
        pkg.providers.add(providesRelation);
    }

    public List<OrDependency> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public Package getPackage() {
        return pkg;
    }

    public Universe getUniverse() {
        return pkg.getUniverse();
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return pkg + " " + version;
    }
}
