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

import org.jboss.up.depres.version.VersionComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Collections.reverseOrder;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class Package {
    private final Universe universe;
    private final String name;
    private final SortedMap<String, PackageVersion> versions = new TreeMap<>(reverseOrder(VersionComparator.INSTANCE));

//    private List<AbstractDependency> fulfillments = new ArrayList<>();

    Collection<BreaksDependency> breakers = new ArrayList<>();
    Collection<Provides> providers = new ArrayList<>();

    Package(final Universe universe, final String name) {
        this.universe = universe;
        this.name = name;
    }

    public PackageVersion addVersion(final String version) {
        assert !versions.containsKey(version) : "Package " + name + " already has version " + version;
        final PackageVersion pkgVersion = new PackageVersion(this, version);
        versions.put(version, pkgVersion);
        return pkgVersion;
    }

    public Collection<BreaksDependency> getBreakers() {
        return Collections.unmodifiableCollection(breakers);
    }

    public String getName() {
        return name;
    }

    public Collection<Provides> getProviders() {
        return Collections.unmodifiableCollection(providers);
    }

    public Universe getUniverse() {
        return universe;
    }

    public SortedMap<String, PackageVersion> getVersions() {
        return Collections.unmodifiableSortedMap(versions);
    }

    @Override
    public String toString() {
        return "package " + name;
    }
}
