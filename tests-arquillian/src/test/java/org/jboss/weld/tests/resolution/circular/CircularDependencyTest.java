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
package org.jboss.weld.tests.resolution.circular;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CircularDependencyTest {

    @Inject
    private Foo foo;

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(CircularDependencyTest.class))
                .addPackage(CircularDependencyTest.class.getPackage())
                .addAsResource(PropertiesBuilder.newBuilder()
                        .set(ConfigurationKey.INJECTABLE_REFERENCE_OPTIMIZATION.get(), "true")
                        .build(), "weld.properties");
    }

    @Test
    public void testCircularInjectionBetweenAppScopedAndDependentBeans() throws Exception {
        foo.getName();
        Assert.assertTrue(Foo.success);
        Assert.assertTrue(Bar.success);
    }

    @Test
    public void testDependentProducerMethodDeclaredOnDependentBeanWhichInjectsProducedBean(
            DependentSelfConsumingDependentProducer producer) throws Exception {
        producer.ping();
    }

    @Test
    public void testDependentSelfConsumingProducer(Violation violation) throws Exception {
        violation.ping();
    }

}
