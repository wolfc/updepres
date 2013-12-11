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
package org.jboss.up.depres.resolver;

import org.jboss.up.depres.*;
import org.jboss.up.depres.Package;
import org.jboss.up.depres.version.VersionComparator;

import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Collections.reverseOrder;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
class PackageMarker {
    final org.jboss.up.depres.Package pkg;
    private final SortedMap<String, PackageVersionMarker> versions = new TreeMap<>(reverseOrder(VersionComparator.INSTANCE));
    private AbstractResolverException failure;

    PackageMarker(final Package pkg) {
        this.pkg = pkg;
    }

    PackageVersionMarker getVersion(final String v) {
        return versions.get(v);
    }

    public boolean hasFailed() {
        return failure != null;
    }

    public void setFailure(final AbstractResolverException failure) {
        this.failure = failure;
    }

    PackageVersionMarker version(final String v) {
        final PackageVersion version = pkg.getVersions().get(v);
        final PackageVersionMarker versionMarker = new PackageVersionMarker(version);
        versions.put(v, versionMarker);
        return versionMarker;
    }
}
