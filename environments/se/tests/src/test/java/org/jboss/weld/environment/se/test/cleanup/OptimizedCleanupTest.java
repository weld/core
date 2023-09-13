/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.cleanup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class OptimizedCleanupTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder().add(
                ShrinkWrap.create(BeanArchive.class).addClasses(OptimizedCleanupTest.class, Foo.class, TestExtension.class))
                .build();
    }

    @Test
    public void testEnabled() {
        TestExtension.PIT_OBSERVED.set(false);
        try (WeldContainer container = new Weld().addExtension(new TestExtension())
                .property(Weld.ALLOW_OPTIMIZED_CLEANUP, Boolean.TRUE)
                .initialize()) {
            BeanManagerImpl beanManager = BeanManagerProxy.unwrap(container.getBeanManager());
            Bean<?> fooBean = beanManager.resolve(beanManager.getBeans(Foo.class));
            assertEquals(ApplicationScoped.class, fooBean.getScope());
            assertTrue(TestExtension.PIT_OBSERVED.get());
            // Container lifecycle event observers should be removed
            assertTrue(beanManager.getObservers().isEmpty());
        }
    }

    @Test
    public void testDisabled() {
        TestExtension.PIT_OBSERVED.set(false);
        try (WeldContainer container = new Weld().addExtension(new TestExtension())
                .property(Weld.ALLOW_OPTIMIZED_CLEANUP, Boolean.FALSE)
                .initialize()) {
            BeanManagerImpl beanManager = BeanManagerProxy.unwrap(container.getBeanManager());
            Bean<?> fooBean = beanManager.resolve(beanManager.getBeans(Foo.class));
            assertEquals(ApplicationScoped.class, fooBean.getScope());
            assertTrue(TestExtension.PIT_OBSERVED.get());
            // Find TestExtension.observeFooPit
            assertTrue(beanManager.getObservers().stream().anyMatch(o -> o.getBeanClass().equals(TestExtension.class)));
        }
    }

}
