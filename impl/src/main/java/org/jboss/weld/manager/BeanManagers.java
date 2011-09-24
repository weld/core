/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class BeanManagers {

    private static class BeanManagerTransform implements Transform<BeanManagerImpl> {

        public static final BeanManagerTransform INSTANCE = new BeanManagerTransform();

        public Iterable<BeanManagerImpl> transform(BeanManagerImpl beanManager) {
            return beanManager.getAccessibleManagers();
        }

    }

    private BeanManagers() {
    }

    public static Set<Iterable<BeanManagerImpl>> getAccessibleClosure(BeanManagerImpl beanManager) {
        Set<Iterable<BeanManagerImpl>> beanManagers = new HashSet<Iterable<BeanManagerImpl>>();
        beanManagers.add(Collections.singleton(beanManager));
        beanManagers.addAll(buildAccessibleClosure(beanManager, BeanManagerTransform.INSTANCE));
        return beanManagers;
    }

    public static <T> Set<Iterable<T>> buildAccessibleClosure(BeanManagerImpl beanManager, Transform<T> transform) {
        Set<Iterable<T>> result = new HashSet<Iterable<T>>();
        buildAccessibleClosure(beanManager, result, new HashSet<BeanManagerImpl>(), transform);
        return result;
    }

    private static <T> void buildAccessibleClosure(BeanManagerImpl beanManager, Set<Iterable<T>> result, Collection<BeanManagerImpl> hierarchy, Transform<T> transform) {
        hierarchy.add(beanManager);
        result.add(transform.transform(beanManager));
        for (BeanManagerImpl accessibleBeanManager : beanManager.getAccessibleManagers()) {
            // Only add if we aren't already in the tree (remove cycles)
            if (!hierarchy.contains(accessibleBeanManager)) {
                buildAccessibleClosure(accessibleBeanManager, result, hierarchy, transform);
            }
        }
    }

}
