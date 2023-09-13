/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.accessibility;

import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class War1Listener extends AbstractTestListener {

    @Inject
    private BeanManager manager;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // validate accessibility from the war1 module
        Set<Bean<?>> accessibleImplementations = manager.getBeans(Animal.class);
        assertEquals(accessibleImplementations.size(), 4);
        assertTrue(containsBean(accessibleImplementations, War1Impl.class));
        assertTrue(containsBean(accessibleImplementations, Library1Impl.class));
        assertTrue(containsBean(accessibleImplementations, SharedLibrary1Impl.class));
        assertTrue(containsBean(accessibleImplementations, SharedLibrary2Impl.class));

        // validate accessibility from the war1 library module
        Bean<?> library1ImplBean = getUniqueBean(accessibleImplementations, Library1Impl.class);
        Library1Impl library1Impl = (Library1Impl) manager.getReference(library1ImplBean, Animal.class,
                manager.createCreationalContext(library1ImplBean));
        BeanManager library1BeanManager = library1Impl.getBeanManager();
        accessibleImplementations = library1BeanManager.getBeans(Animal.class);
        assertEquals(accessibleImplementations.size(), 4);
        assertTrue(containsBean(accessibleImplementations, War1Impl.class));
        assertTrue(containsBean(accessibleImplementations, Library1Impl.class));
        assertTrue(containsBean(accessibleImplementations, SharedLibrary1Impl.class));
        assertTrue(containsBean(accessibleImplementations, SharedLibrary2Impl.class));

        // validate accessibility from the shared library 1
        Bean<?> sharedLibrary1ImplBean = getUniqueBean(accessibleImplementations, SharedLibrary1Impl.class);
        SharedLibrary1Impl sharedLibrary1Impl = (SharedLibrary1Impl) manager.getReference(sharedLibrary1ImplBean, Animal.class,
                manager.createCreationalContext(sharedLibrary1ImplBean));
        BeanManager sharedLibrary1BeanManager = sharedLibrary1Impl.getBeanManager();
        accessibleImplementations = sharedLibrary1BeanManager.getBeans(Animal.class);
        assertEquals(accessibleImplementations.size(), 2); // implementations within wars are not accessible
        assertTrue(containsBean(accessibleImplementations, SharedLibrary1Impl.class));
        assertTrue(containsBean(accessibleImplementations, SharedLibrary2Impl.class));

        // validate accessibility from the shared library 2
        Bean<?> sharedLibrary2ImplBean = getUniqueBean(accessibleImplementations, SharedLibrary2Impl.class);
        SharedLibrary2Impl sharedLibrary2Impl = (SharedLibrary2Impl) manager.getReference(sharedLibrary2ImplBean, Animal.class,
                manager.createCreationalContext(sharedLibrary2ImplBean));
        BeanManager sharedLibrary2BeanManager = sharedLibrary2Impl.getBeanManager();
        accessibleImplementations = sharedLibrary2BeanManager.getBeans(Animal.class);
        assertEquals(accessibleImplementations.size(), 2); // implementations within wars are not accessible
        assertTrue(containsBean(accessibleImplementations, SharedLibrary1Impl.class));
        assertTrue(containsBean(accessibleImplementations, SharedLibrary2Impl.class));
    }
}
