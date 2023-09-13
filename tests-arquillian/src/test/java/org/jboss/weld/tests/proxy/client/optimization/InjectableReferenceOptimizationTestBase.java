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
package org.jboss.weld.tests.proxy.client.optimization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.PropertiesBuilder;

/**
 *
 * @author Martin Kouba
 * @see WELD-1659
 */
public abstract class InjectableReferenceOptimizationTestBase {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(InjectableReferenceOptimizationTestBase.class))
                .addClasses(InjectableReferenceOptimizationTestBase.class, Utils.class, Alpha.class, Bravo.class,
                        Charlie.class, Delta.class, Echo.class, Foxtrot.class, Golf.class, Hotel.class, India.class,
                        Custom.class, CustomScoped.class, CustomContext.class, CustomScopeExtension.class)
                .addAsServiceProvider(Extension.class, CustomScopeExtension.class)
                .addAsResource(
                        PropertiesBuilder.newBuilder().set(ConfigurationKey.INJECTABLE_REFERENCE_OPTIMIZATION.get(), "true")
                                .build(),
                        "weld.properties");
    }

    protected void assertIsProxy(Object beanInstance) {
        assertProxy(beanInstance, true);
    }

    protected void assertIsNotProxy(Object beanInstance) {
        assertProxy(beanInstance, false);
    }

    protected void assertProxy(Object beanInstance, boolean expectsProxy) {
        assertNotNull(beanInstance);
        if (expectsProxy) {
            assertTrue(beanInstance + " is not a proxy", Utils.isProxy(beanInstance));
        } else {
            assertFalse(beanInstance + " is a proxy", Utils.isProxy(beanInstance));
        }
    }

}
