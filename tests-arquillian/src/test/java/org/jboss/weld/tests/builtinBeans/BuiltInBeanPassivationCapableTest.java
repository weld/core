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
package org.jboss.weld.tests.builtinBeans;

import static org.jboss.weld.tests.builtinBeans.Checker.checkBeanManager;
import static org.jboss.weld.tests.builtinBeans.Checker.checkEquality;
import static org.jboss.weld.tests.builtinBeans.Checker.checkEvent;
import static org.jboss.weld.tests.builtinBeans.Checker.checkInjectionPoint;
import static org.jboss.weld.tests.builtinBeans.Checker.checkInstance;
import static org.jboss.weld.tests.builtinBeans.Checker.checkPrincipal;
import static org.jboss.weld.tests.builtinBeans.Checker.checkUserTransaction;

import java.security.Principal;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Broken;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BuiltInBeanPassivationCapableTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(BuiltInBeanPassivationCapableTest.class))
                .intercept(FooInterceptor.class)
                // WELD-1048
                .decorate(AnimalDecorator.class)
                .addPackage(BuiltInBeanPassivationCapableTest.class.getPackage())
                .addClass(Utils.class);
    }

    @Test
    @Category({ Integration.class, Broken.class })
    public void testPrincipal(Principal principal) throws Throwable {
        Principal principal1 = Utils.deserialize(Utils.serialize(principal));
        Assert.assertTrue(checkPrincipal(principal1));
    }

    @Test
    public void testUserTransactionBean(UserTransaction userTransaction) throws Throwable {
        // proxy for this class will be declared using our CL hence the deserialization also needs to use the same CL
        UserTransaction userTransaction1 = Utils.deserialize(Utils.serialize(userTransaction),
                userTransaction.getClass().getClassLoader());
        Assert.assertTrue(checkUserTransaction(userTransaction1));
    }

    @Test
    public void testBeanManagerBean(BeanManager beanManager) throws Throwable {
        BeanManager beanManager1 = Utils.deserialize(Utils.serialize(beanManager));
        Assert.assertTrue(checkBeanManager(beanManager1));
        Assert.assertTrue(checkEquality(beanManager, beanManager1));
    }

    @Test
    public void testInstance(Consumer consumer) throws Throwable {
        Instance<Cow> instance = consumer.getCow();
        Instance<Cow> instance1 = Utils.deserialize(Utils.serialize(instance));
        Assert.assertTrue(checkInstance(instance1));
        Assert.assertTrue(checkEquality(instance, instance1));
    }

    @Test
    public void testEvent(Consumer consumer, CowEventObserver observer) throws Throwable {
        Event<Cow> event = consumer.getEvent();
        Event<Cow> event1 = Utils.deserialize(Utils.serialize(event));
        Assert.assertTrue(checkEvent(event1, observer));
        Assert.assertTrue(checkEquality(event, event1));
    }

    @Test
    public void testFieldInjectionPoint(FieldInjectionPointConsumer consumer) throws Throwable {
        Dog.reset();
        consumer.ping();
        InjectionPoint injectionPoint = Dog.getInjectionPoint();
        InjectionPoint injectionPoint1 = Utils.deserialize(Utils.serialize(injectionPoint));
        Assert.assertTrue(checkInjectionPoint(injectionPoint1, FieldInjectionPointConsumer.class));
        Assert.assertTrue(checkEquality(injectionPoint, injectionPoint1));
    }

    @Test
    public void testConstructorInjectionPoint(ConstructorInjectionPointConsumer consumer) throws Throwable {
        Dog.reset();
        consumer.ping();
        InjectionPoint injectionPoint = Dog.getInjectionPoint();
        InjectionPoint injectionPoint1 = Utils.deserialize(Utils.serialize(injectionPoint));
        Assert.assertTrue(checkInjectionPoint(injectionPoint1, ConstructorInjectionPointConsumer.class));
        Assert.assertTrue(checkEquality(injectionPoint, injectionPoint1));
    }

    @Test
    public void testMethodInjectionPoint(MethodInjectionPointConsumer consumer) throws Throwable {
        Dog.reset();
        consumer.ping();
        InjectionPoint injectionPoint = Dog.getInjectionPoint();
        InjectionPoint injectionPoint1 = Utils.deserialize(Utils.serialize(injectionPoint));
        Assert.assertTrue(checkInjectionPoint(injectionPoint1, MethodInjectionPointConsumer.class));
        Assert.assertTrue(checkEquality(injectionPoint, injectionPoint1));
    }

    @Test
    public void testAllOnBean(Consumer consumer) throws Throwable {
        consumer.check();
        Consumer consumer1 = Utils.deserialize(Utils.serialize(consumer));
        consumer1.check();
    }

    @Test
    public void testInjectedBeanMetadata(Sheep sheep) throws Throwable {
        Utils.deserialize(Utils.serialize(sheep));
    }
}
