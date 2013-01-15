/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            return 1;
        else if (c2.isAssignableFrom(c1))
            return -1;
        else
            return 0;
    }
}
