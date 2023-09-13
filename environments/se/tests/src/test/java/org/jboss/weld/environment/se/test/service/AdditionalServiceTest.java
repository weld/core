/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies, that additional {@link Service} implementations may be registered.
 *
 * @author Jozef Hartinger
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class AdditionalServiceTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        @SuppressWarnings("rawtypes")
        Class[] classes = new Class[] {
                AdditionalServiceTest.class,
                AlphaImpl.class,
                AlphaService.class,
                Bravo1Service.class,
                Bravo2Service.class,
                BravoImpl.class,
                ExecutorServices1.class,
                ExecutorServices2.class,
                DummyBean.class
        };
        final JavaArchive bda1 = ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(AdditionalServiceTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(classes).addAsServiceProvider(Service.class, ExecutorServices1.class);
        return ClassPath.builder().add(bda1).build();
    }

    @Test
    public void testSimpleAdditionalService() {
        try (WeldContainer container = createWeld().addServices(new AlphaImpl()).initialize()) {
            ServiceRegistry registry = BeanManagerProxy.unwrap(container.getBeanManager()).getServices();
            assertNotNull(registry.get(AlphaService.class));
            assertTrue(registry.get(AlphaService.class) instanceof AlphaImpl);
            assertNull(registry.get(AlphaImpl.class));
        }
    }

    @Test
    public void testAdditionalServiceWithMultipleInterfaces() {
        try (WeldContainer container = createWeld().addServices(new BravoImpl()).initialize()) {
            ServiceRegistry registry = BeanManagerProxy.unwrap(container.getBeanManager()).getServices();
            Bravo1Service bravo1 = registry.get(Bravo1Service.class);
            Bravo2Service bravo2 = registry.get(Bravo2Service.class);
            BravoImpl bravo3 = registry.get(BravoImpl.class);
            assertNotNull(bravo1);
            assertNotNull(bravo2);
            assertNotNull(bravo3);
            assertTrue(bravo1 == bravo2);
            assertTrue(bravo2 == bravo3);
        }
    }

    @Test
    public void testOverridingService() {
        try (WeldContainer container = createWeld().addServices(new ExecutorServices2()).initialize()) {
            ServiceRegistry registry = BeanManagerProxy.unwrap(container.getBeanManager()).getServices();
            ExecutorServices executorServices = registry.get(ExecutorServices.class);
            Assert.assertNotNull(executorServices);
            Assert.assertTrue(executorServices instanceof ExecutorServices2);
        }
    }

    private Weld createWeld() {
        return new Weld().property(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), false);
    }
}
