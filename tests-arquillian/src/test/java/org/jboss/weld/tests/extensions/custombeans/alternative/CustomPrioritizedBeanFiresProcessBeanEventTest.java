/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.extensions.custombeans.alternative;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that registering a synthetic enabled alternative via {@code Bean<T>} that implements {@code Prioritized} will
 * fire {@code ProcessBean} event.
 */
@RunWith(Arquillian.class)
public class CustomPrioritizedBeanFiresProcessBeanEventTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(CustomPrioritizedBeanFiresProcessBeanEventTest.class))
                .addClasses(Foo.class, MyExtension.class, FooBean.class)
                .addAsServiceProvider(Extension.class, MyExtension.class);
    }

    @Inject
    Foo foo;

    @Test
    public void testBeanTriggeredEvents() {
        Assert.assertEquals(2, MyExtension.PB_TRIGGERED);
        Assert.assertEquals(1, MyExtension.PSB_TRIGGERED);
        Assert.assertEquals(FooBean.class.getSimpleName(), foo.ping());
    }
}
