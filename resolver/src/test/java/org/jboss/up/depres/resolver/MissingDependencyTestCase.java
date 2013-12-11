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

import org.jboss.up.depres.Universe;
import org.jboss.up.depres.dpkg.AvailableReader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.jboss.up.depres.resolver.Util.universe;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class MissingDependencyTestCase {
    @Test
    public void testMissingDependency() throws Exception {
        final Universe universe = universe("available-missing-dep.txt");
        final Resolver resolver = new Resolver(universe);
        try {
            resolver.install(universe.pkg("A"));
            fail("Should have thrown " + PackageUnavailableException.class.getSimpleName());
        } catch (PackageUnavailableException e) {
            // good
            assertEquals("C", e.getMessage());
        }
    }

    @Test
    public void testMissingDependency2() throws Exception {
        final Universe universe = universe("available-missing-dep2.txt");
        final Resolver resolver = new Resolver(universe);
        resolver.install(universe.pkg("A"));
    }
}
