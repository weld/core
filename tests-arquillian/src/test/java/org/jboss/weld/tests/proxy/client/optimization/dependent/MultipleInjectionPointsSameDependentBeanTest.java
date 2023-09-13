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
package org.jboss.weld.tests.proxy.client.optimization.dependent;

import static org.junit.Assert.assertFalse;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test should verify that enabled injectable reference lookup optimization does not break multiple injection points of the
 * same dependent managed bean.
 *
 * @author Martin Kouba
 * @see WELD-1810
 */
@RunWith(Arquillian.class)
public class MultipleInjectionPointsSameDependentBeanTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(MultipleInjectionPointsSameDependentBeanTest.class))
                .addClasses(FooRequest.class, FooApplication.class, Bar.class)
                .addAsResource(
                        PropertiesBuilder.newBuilder().set(ConfigurationKey.INJECTABLE_REFERENCE_OPTIMIZATION.get(), "true")
                                .build(),
                        "weld.properties");
    }

    @Test
    public void testOptimizationDoesNotAffectDependentBeans(FooRequest fooRequest, FooApplication fooApplication) {
        assertFalse(fooRequest.getBar1().getId().equals(fooRequest.getBar2().getId()));
        assertFalse(fooApplication.getBar1().getId().equals(fooApplication.getBar2().getId()));
    }

}
