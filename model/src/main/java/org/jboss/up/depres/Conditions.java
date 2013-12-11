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

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class Conditions {
    public static Condition create(final String expr) {
        final int i = expr.indexOf(' ');
        if (i == -1)
            throw new IllegalArgumentException("Illegal expression " + expr);
        final String operator = expr.substring(0, i);
        final String data = expr.substring(i + 1);
        if (operator.equals("="))
            return new AbstractCondition(data) {
                @Override
                public boolean matches(String data) {
                    return VersionComparator.INSTANCE.compare(this.data, data) == 0;
                }
            };
        if (operator.equals("<<"))
            return new AbstractCondition(data) {
                @Override
                public boolean matches(String data) {
                    return VersionComparator.INSTANCE.compare(this.data, data) > 0;
                }
            };
        if (operator.equals(">>"))
            return new AbstractCondition(data) {
                @Override
                public boolean matches(String data) {
                    return VersionComparator.INSTANCE.compare(this.data, data) < 0;
                }
            };
        if (operator.equals("<="))
            return new AbstractCondition(data) {
                @Override
                public boolean matches(String data) {
                    return VersionComparator.INSTANCE.compare(this.data, data) >= 0;
                }
            };
        if (operator.equals(">="))
            return new AbstractCondition(data) {
                @Override
                public boolean matches(String data) {
                    return VersionComparator.INSTANCE.compare(this.data, data) <= 0;
                }
            };
        throw new RuntimeException("NYI: operator " + operator);
    }
}
