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
package org.jboss.weld.tests.contexts.alterable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for https://issues.jboss.org/browse/CDI-139
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class AlterableContextTest {

    private static final String[] VALUES = { "foo", "bar", "baz" };

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(AlterableContextTest.class))
                .addPackage(AlterableContextTest.class.getPackage())
                .addAsServiceProvider(Extension.class, CustomScopeExtension.class);
    }

    @Inject
    private BeanManager manager;

    @Test
    public void testApplicationScopedComponent() {
        testComponent(ApplicationScopedComponent.class);
    }

    @Test
    public void testRequestScopedComponent() {
        testComponent(RequestScopedComponent.class);
    }

    @Test
    public void testCustomScopedComponent() {
        testComponent(CustomScopedComponent.class);
    }

    @Test
    public void testNothingHappensIfNoInstanceToDestroy(ApplicationScopedComponent application) {
        Bean<?> bean = manager.resolve(manager.getBeans(ApplicationScopedComponent.class));
        AlterableContext context = (AlterableContext) manager.getContext(bean.getScope());

        AbstractComponent.reset();
        application.setValue("value");
        context.destroy(bean);
        assertTrue(AbstractComponent.isDestroyed());

        context.destroy(bean); // make sure subsequent calls do not raise exception
        context.destroy(bean);
    }

    private <T extends AbstractComponent> void testComponent(Class<T> javaClass) {
        Bean<?> bean = manager.resolve(manager.getBeans(javaClass));
        @SuppressWarnings("unchecked")
        T reference = (T) manager.getReference(bean, javaClass, manager.createCreationalContext(bean));
        AlterableContext context = (AlterableContext) manager.getContext(bean.getScope());

        for (String string : VALUES) {
            assertNull(reference.getValue());
            reference.setValue(string);
            assertEquals(string, reference.getValue());

            AbstractComponent.reset();
            context.destroy(bean);
            assertTrue(AbstractComponent.isDestroyed());
            assertNull(reference.getValue(), reference.getValue());
        }
    }
}
