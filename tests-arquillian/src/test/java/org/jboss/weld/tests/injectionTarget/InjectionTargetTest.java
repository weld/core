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
package org.jboss.weld.tests.injectionTarget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.exceptions.CreationException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldInjectionTargetBuilder;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InjectionTargetTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InjectionTargetTest.class)).addPackage(InjectionTargetTest.class.getPackage())
                .addClass(Utils.class);
    }

    /*
     * description = "WELD-763"
     */
    @Test
    public void testCreateInjectionTargetOfInterface(BeanManager beanManager) {
        try {
            beanManager.getInjectionTargetFactory(beanManager.createAnnotatedType(Foo.class)).createInjectionTarget(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail();
    }

    @Test
    public void testObtainingInjectionTargetForAbstractClass(BeanManager beanManager) {
        InjectionTarget<AbstractClass> it = beanManager.getInjectionTargetFactory(beanManager.createAnnotatedType(AbstractClass.class))
                .createInjectionTarget(null);
        CreationalContext<AbstractClass> ctx = beanManager.createCreationalContext(null);
        AbstractClass instance = new AbstractClass() {
        };
        it.postConstruct(instance);
        it.inject(instance, ctx);
        assertNotNull(instance.getManager());
        try {
            it.produce(ctx);
            Assert.fail();
        } catch (CreationException expected) {
        }
        it.preDestroy(instance);
    }

    @Test
    public void testObtainingInjectionTarget(BeanManager beanManager) {
        InjectionTarget<NonStaticInnerClass> it = beanManager.getInjectionTargetFactory(beanManager.createAnnotatedType(NonStaticInnerClass.class))
                .createInjectionTarget(null);
        CreationalContext<NonStaticInnerClass> ctx = beanManager.createCreationalContext(null);
        NonStaticInnerClass instance = new NonStaticInnerClass();
        it.postConstruct(instance);
        it.inject(instance, ctx);
        assertNotNull(instance.getEvent());
        try {
            it.produce(ctx);
            Assert.fail();
        } catch (CreationException expected) {
        }
        it.preDestroy(instance);
    }

    @Test
    public void testNonProducibleInjectionTargetBuilderForClassWithoutBeanConstructor(BeanManagerImpl beanManager) {
        WeldInjectionTargetBuilder<Bar> builder = beanManager.createInjectionTargetBuilder(beanManager.createAnnotatedType(Bar.class));
        InjectionTarget<Bar> injectionTarget = builder.build();
        CreationalContext<Bar> ctx = beanManager.createCreationalContext(null);

        Bar instance = new Bar("foo");
        injectionTarget.postConstruct(instance);
        assertEquals("fooinit", instance.getId());
        assertNull(instance.getBeanManager());
        injectionTarget.inject(instance, ctx);
        assertNotNull(instance.getBeanManager());
        try {
            injectionTarget.produce(ctx);
            Assert.fail();
        } catch (CreationException expected) {
        }
    }

    @Test
    public void testNonProducibleInjectionTargetBuilderForAbstractClass(BeanManagerImpl beanManager) {
        WeldInjectionTargetBuilder<Baz> builder = beanManager.createInjectionTargetBuilder(beanManager.createAnnotatedType(Baz.class));
        InjectionTarget<Baz> injectionTarget = builder.build();
        CreationalContext<Baz> ctx = beanManager.createCreationalContext(null);

        Baz instance = new Baz() {};
        injectionTarget.postConstruct(instance);
        assertEquals("init", instance.getId());
        assertNull(instance.getBeanManager());
        injectionTarget.inject(instance, ctx);
        assertNotNull(instance.getBeanManager());
        try {
            injectionTarget.produce(ctx);
            Assert.fail();
        } catch (CreationException expected) {
        }
    }

    private class NonStaticInnerClass {
        @Inject
        private Event<String> event;

        public Event<String> getEvent() {
            return event;
        }
    }
}
