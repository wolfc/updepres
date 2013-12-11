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

import org.jboss.up.depres.PackageVersion;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
class PackageVersionMarker {
    private final PackageVersion packageVersion;
    private AbstractResolverException failure;

    PackageVersionMarker(PackageVersion packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getVersion() {
        return packageVersion.getVersion();
    }

    public boolean hasFailed() {
        return failure != null;
    }

    public void setFailure(final AbstractResolverException failure) {
        this.failure = failure;
    }

    @Override
    public String toString() {
        return "PackageVersionMarker{" +
                "packageVersion=" + packageVersion +
                ", failure=" + failure +
                '}';
    }
}
