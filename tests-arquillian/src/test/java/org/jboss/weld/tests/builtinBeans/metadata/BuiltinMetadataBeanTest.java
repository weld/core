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
package org.jboss.weld.tests.builtinBeans.metadata;

import static org.jboss.weld.util.reflection.Reflections.cast;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Type;
import java.util.Collections;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BuiltinMetadataBeanTest {

    @Inject
    private BeanManager manager;
    @Inject
    private Yoghurt yoghurt;
    @Inject
    private YoghurtFactory factory;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(BuiltinMetadataBeanTest.class))
                .intercept(YoghurtInterceptor.class).decorate(MilkProductDecorator.class)
                .addPackage(Yoghurt.class.getPackage());
    }

    @Test
    public void testBeanMetadata() {
        Bean<?> resolvedBean = manager.resolve(manager.getBeans(Yoghurt.class));
        assertNotNull(resolvedBean);
        assertEquals(resolvedBean, yoghurt.getBeanBean());
    }

    @Test
    @SuppressWarnings("unused")
    public void testProducerMethodMetadata() {
        Bean<Yoghurt> fruitYoghurtBean = cast(manager.resolve(manager.getBeans(Yoghurt.class, new Fruit.Literal())));
        CreationalContext<Yoghurt> fruitCtx = manager.createCreationalContext(fruitYoghurtBean);
        Yoghurt fruitYoghurt = cast(manager.getReference(fruitYoghurtBean, Yoghurt.class, fruitCtx));
        assertEquals(fruitYoghurtBean, factory.getFruitYoghurtBean());

        Bean<Yoghurt> probioticYoghurtBean = cast(manager.resolve(manager.getBeans(Yoghurt.class, new Probiotic.Literal())));
        CreationalContext<Yoghurt> probioticCtx = manager.createCreationalContext(probioticYoghurtBean);
        Yoghurt probioticYoghurt = cast(manager.getReference(probioticYoghurtBean, Yoghurt.class, probioticCtx));
        assertEquals(probioticYoghurtBean, factory.getProbioticYoghurtBean());
    }

    @Test
    public void testInterceptorMetadata() {
        Bean<?> bean = manager.resolve(manager.getBeans(Yoghurt.class));
        Interceptor<?> interceptor = manager.resolveInterceptors(InterceptionType.AROUND_INVOKE, new Frozen.Literal())
                .iterator().next();
        YoghurtInterceptor instance = yoghurt.getInterceptorInstance();
        assertEquals(interceptor, instance.getBean());
        assertEquals(interceptor, instance.getInterceptor());
        assertEquals(bean, instance.getInterceptedBean());
    }

    @Test
    public void testDecoratorMetadata() {
        Bean<?> bean = manager.resolve(manager.getBeans(Yoghurt.class));
        Decorator<?> decorator = manager.resolveDecorators(Collections.<Type> singleton(MilkProduct.class)).iterator().next();
        MilkProductDecorator instance = yoghurt.getDecoratorInstance();
        assertEquals(decorator, instance.getBean());
        assertEquals(decorator, instance.getDecorator());
        assertEquals(bean, instance.getDecoratedBean());
    }

    @Test
    public void testIllegalInjectionDetected() {
        testIllegalInjection(Bean.class);
        testIllegalInjection(Interceptor.class);
        testIllegalInjection(Decorator.class);
    }

    private void testIllegalInjection(Class<?> type) {
        Bean<?> bean = manager.resolve(manager.getBeans(type));
        CreationalContext<?> ctx = manager.createCreationalContext(bean);
        try {
            manager.getReference(bean, type, ctx);
            Assert.fail();
        } catch (IllegalArgumentException expected) {
        }
    }
}
