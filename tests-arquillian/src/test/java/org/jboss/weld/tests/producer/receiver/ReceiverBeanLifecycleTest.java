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
package org.jboss.weld.tests.producer.receiver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * CDI spec 6.4.2:
 * "any @Dependent scoped contextual instance created to receive a producer method, producer field, disposer method or observer
 * method invocation is destroyed when the invocation completes, and"
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class ReceiverBeanLifecycleTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ReceiverBeanLifecycleTest.class))
                .addPackage(ReceiverBeanLifecycleTest.class.getPackage()).addClass(Utils.class);
    }

    @Test
    public void testDependentReceiverInstanceDestroyedAfterProducerFieldInvocation(BeanManager manager) {
        Producer.reset();

        Product1 product = Utils.getReference(manager, Utils.<Product1> getBean(manager, Product1.class), Product1.class);
        assertNotNull(product);

        assertTrue(Producer.isDestroyed());
    }

    @Test
    public void testDependentReceiverInstanceDestroyedAfterProducerMethodInvocation(BeanManager manager) {
        Producer.reset();
        Dependency.reset();

        Bean<Product2> bean = Reflections.cast(manager.resolve(manager.getBeans(Product2.class)));
        CreationalContext<Product2> cc = manager.createCreationalContext(bean);
        Product2 instance = bean.create(cc);

        assertNotNull(instance);

        assertTrue(Producer.isDestroyed());
        assertFalse(Dependency.isDestroyed());

        bean.destroy(instance, cc);

        assertTrue(Dependency.isDestroyed());
    }
}
