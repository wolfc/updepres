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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class Resolver {
    private final Universe universe;
    private final Map<String, PackageMarker> markers = new HashMap<>();

    public Resolver(Universe universe) {
        this.universe = universe;
    }

    public void install(final org.jboss.up.depres.Package pkg) throws AbstractResolverException {
        final String name = pkg.getName();
        PackageMarker marker = markers.get(name);
        // already considered for installation?
        if (marker != null)
            return;
        marker = new PackageMarker(pkg);
        markers.put(name, marker);

        try {
            installOneVersion(marker);
        } catch (AbstractResolverException e) {
            marker.setFailure(e);
            // TODO: reverse traverse the dependencies to unmark them for installation
            throw e;
        }
    }

    public void install(final PackageVersion packageVersion) throws AbstractResolverException {
        final Package pkg = packageVersion.getPackage();
        final String name = pkg.getName();
        PackageMarker marker = markers.get(name);
        // already considered for installation?
        if (marker != null) {
            throw new RuntimeException("NYI");
        }
        install(pkg);
    }

    private void installOneDependency(final OrDependency orDependency) throws AbstractResolverException {
        Collection<PackageUnavailableException> failures = null;
        for (Dependency dependency : orDependency.getDependencies()) {
            System.out.println("  " + dependency);
            try {
                install(dependency.getProvider());
                return;
            } catch (PackageUnavailableException e) {
                failures = store(failures, e);
            }
        }
        if (failures != null) {
            if (failures.size() == 1)
                throw failures.iterator().next();
            throw new DependencyInstallationFailedException(failures);
        }
    }

    private void installOneVersion(final PackageMarker marker) throws AbstractResolverException {
        Collection<AbstractResolverException> failures = null;
        final Package pkg = marker.pkg;
        final SortedMap<String, PackageVersion> versions = pkg.getVersions();
        if (versions.size() == 0)
            throw new PackageUnavailableException(pkg.getName());
        for (PackageVersion packageVersion : pkg.getVersions().values()) {
            final PackageVersionMarker packageVersionMarker = marker.version(packageVersion.getVersion());
            try {
                for (BreaksDependency breaker : pkg.getBreakers()) {
                    final PackageMarker originator = markers.get(breaker.getOriginator().getPackage().getName());
                    if (originator == null)
                        continue;
                    // do we still want the originator installed?
                    if (originator.hasFailed())
                        continue;
                    if (breaker.getCondition().matches(packageVersionMarker.getVersion())) {
                        throw new BrokenByException("Can't install " + packageVersionMarker + ", it is broken by " + breaker.getOriginator());
                    }
                }

                System.out.println("Installing " + packageVersionMarker);
                for (Provides provider : pkg.getProviders()) {
                    // TODO: need to choose one version
                    install(provider.getOriginator());
                }
                for (OrDependency orDependency : packageVersion.getDependencies()) {
                    installOneDependency(orDependency);
                }
                return;
            } catch (AbstractResolverException e) {
                packageVersionMarker.setFailure(e);
                // TODO: reverse traverse
                failures = store(failures, e);
            }
        }
        if (failures == null)
            throw new IllegalStateException("NYI"); // should never happen
        if (failures.size() == 1)
            throw failures.iterator().next();
        throw new InstallationFailedException(failures);
    }

    private static <E> Collection<E> store(Collection<E> store, final E element) {
        if (store == null)
            store = new ArrayList<>();
        store.add(element);
        return store;
    }
}
