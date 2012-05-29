/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.builtinBeans.metadata;

import static org.jboss.weld.util.reflection.Reflections.cast;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
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
        return ShrinkWrap.create(BeanArchive.class).intercept(YoghurtInterceptor.class).decorate(MilkProductDecorator.class)
                .addPackage(Yoghurt.class.getPackage());
    }

    @Test
    public void testBeanMetadata() {
        Bean<?> resolvedBean = manager.resolve(manager.getBeans(Yoghurt.class));
        assertNotNull(resolvedBean);
        assertEquals(resolvedBean, yoghurt.getBeanBean());
    }

    @Test
    public void testProducerAndDisposerMethodMetadata() {
        Bean<Yoghurt> fruitYoghurtBean = cast(manager.resolve(manager.getBeans(Yoghurt.class, new Fruit.Literal())));
        CreationalContext<Yoghurt> fruitCtx = manager.createCreationalContext(fruitYoghurtBean);
        Yoghurt fruitYoghurt = cast(manager.getReference(fruitYoghurtBean, Yoghurt.class, fruitCtx));
        assertEquals(fruitYoghurtBean, factory.getFruitYoghurtBean());

        Bean<Yoghurt> probioticYoghurtBean = cast(manager.resolve(manager.getBeans(Yoghurt.class, new Probiotic.Literal())));
        CreationalContext<Yoghurt> probioticCtx = manager.createCreationalContext(probioticYoghurtBean);
        Yoghurt probioticYoghurt = cast(manager.getReference(probioticYoghurtBean, Yoghurt.class, fruitCtx));
        assertEquals(probioticYoghurtBean, factory.getProbioticYoghurtBean());

        // now verify the disposer method
        fruitYoghurtBean.destroy(fruitYoghurt, fruitCtx);
        probioticYoghurtBean.destroy(probioticYoghurt, probioticCtx);
        List<Bean<?>> beans = factory.getBeans();
        assertEquals(2, beans.size());
        assertEquals(fruitYoghurtBean, factory.getBeans().get(0));
        assertEquals(probioticYoghurtBean, factory.getBeans().get(1));
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
        assertNull(getReference(Bean.class));
        assertNull(getReference(Interceptor.class));
        assertNull(getReference(Decorator.class));
    }

    private Object getReference(Class<?> type) {
        Bean<?> bean = manager.resolve(manager.getBeans(type));
        CreationalContext<?> ctx = manager.createCreationalContext(bean);
        return manager.getReference(bean, type, ctx);
    }
}
