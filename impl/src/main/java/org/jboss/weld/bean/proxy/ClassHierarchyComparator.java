/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.bean.proxy;

import java.util.Comparator;

/**
 * Class hierarchy comparator.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class ClassHierarchyComparator implements Comparator<Class<?>> {

    static final ClassHierarchyComparator INSTANCE = new ClassHierarchyComparator();

    public int compare(Class<?> c1, Class<?> c2) {
        if (c1.equals(c2))
            return 0;
        else if (c1.isAssignableFrom(c2))
            return -1;
        else if (c2.isAssignableFrom(c1))
            return 1;
        else
            return 0;
    }
}
