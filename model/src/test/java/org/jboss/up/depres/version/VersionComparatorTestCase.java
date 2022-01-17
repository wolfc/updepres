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
package org.jboss.up.depres.version;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class VersionComparatorTestCase {
    private static final VersionComparator VERSION_COMPARATOR = new VersionComparator();

    @Test
    public void test1() {
        assertTrue(VERSION_COMPARATOR.compare("1.0.GA", "1.0.1.GA") < 0);
    }

    @Test
    public void testAlphaBeta() {
        assertTrue(VERSION_COMPARATOR.compare("1.0.0.Alpha", "1.0.0.Beta") < 0);
    }

    @Test
    public void testEpoch() {
        assertTrue(VERSION_COMPARATOR.compare("0:1", "1:1") < 0);
    }

    @Test
    public void testIssue1() {
        System.out.println("** " + VERSION_COMPARATOR.compare("2.1.9.redhat-001", "2.1.9.redhat-1"));
        assertTrue(VERSION_COMPARATOR.compare("2.1.9.redhat-001", "2.1.9.redhat-1") < 0);
    }

    @Test
    public void testIssue1b() {
        System.out.println("** " + VERSION_COMPARATOR.compare("2.1.9.redhat-001", "2.1.9.redhat-2"));
        assertTrue(VERSION_COMPARATOR.compare("2.1.9.redhat-001", "2.1.9.redhat-2") < 0);
    }

    @Test
    public void testIssue1c() {
        System.out.println("** " + VERSION_COMPARATOR.compare("2.1.9.redhat-1", "2.1.9.redhat-002"));
        assertTrue(VERSION_COMPARATOR.compare("2.1.9.redhat-1", "2.1.9.redhat-002") < 0);
    }

    @Test
    public void version10isLargerThen1() {
        assertTrue(VERSION_COMPARATOR.compare("1.0", "10.0") < 0);
        assertTrue(VERSION_COMPARATOR.compare("1.1", "1.10") < 0);
    }
}
