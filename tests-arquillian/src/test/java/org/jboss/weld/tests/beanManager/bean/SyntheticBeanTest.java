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
package org.jboss.weld.tests.beanManager.bean;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.literal.AnyLiteral;
import static org.jboss.weld.util.reflection.Reflections.cast;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SyntheticBeanTest {

    @Inject
    private BeanManager manager;

    @Inject
    @Large
    private Office office;

    @Inject
    private SerializableOffice serializableOffice;

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).intercept(SimpleInterceptor.class, LifecycleInterceptor.class).decorate(VehicleDecorator.class).addPackage(Simple.class.getPackage())
                .addAsServiceProvider(Extension.class, BeanExtension.class);
    }

    @Test
    public void testRegisteredBean() {
        Bean<Office> bean = cast(manager.resolve(manager.getBeans(Office.class, Large.Literal.INSTANCE)));
        assertEquals(3, bean.getInjectionPoints().size());
        for (InjectionPoint ip : bean.getInjectionPoints()) {
            assertEquals(bean, ip.getBean());
        }
    }

    @Test
    public void testSyntheticBeanConstructedProperly() {
        Bean<Office> bean = cast(manager.resolve(manager.getBeans(Office.class, Large.Literal.INSTANCE)));
        testOffice(bean);
    }

    @Test
    public void testSerializableBean() {
        Bean<Office> bean = cast(manager.resolve(manager.getBeans(SerializableOffice.class, AnyLiteral.INSTANCE)));
        assertTrue(bean instanceof PassivationCapable);
        testOffice(bean);
    }

    @Test
    public void testSyntheticBeanIntercepted() {
        assertTrue(office.intercepted());
        assertTrue(serializableOffice.intercepted());

        LifecycleInterceptor.reset();
        Bean<Office> bean = cast(manager.resolve(manager.getBeans(Office.class, Large.Literal.INSTANCE)));
        CreationalContext<Office> ctx = manager.createCreationalContext(bean);
        Office instance = bean.create(ctx);
        instance.intercepted();
        bean.destroy(instance, ctx);
        assertTrue(LifecycleInterceptor.isPostConstructCalled());
        assertTrue(LifecycleInterceptor.isPreDestroyCalled());
    }

    private void testOffice(Bean<Office> bean) {
        CreationalContext<Office> ctx = manager.createCreationalContext(bean);
        Office office = (Office) bean.create(ctx);
        assertNotNull(office);
        assertNotNull(office.getConstructorEmployee());
        assertNotNull(office.getFieldEmployee());
        assertNotNull(office.getInitializerEmployee());
        assertTrue(office.isPostConstructCalled());
        bean.destroy(office, ctx);
        assertTrue(Office.isPreDestroyCalled());
    }

    @Test
    public void testSyntheticProducerField(@Hungry Lion lion) {
        assertNotNull(lion);
        lion.foo();
        Bean<Lion> bean = cast(manager.resolve(manager.getBeans(Lion.class, Hungry.Literal.INSTANCE)));
        assertTrue(bean.getQualifiers().contains(Hungry.Literal.INSTANCE));
    }
    
    @Test
    public void testSyntheticProducerMethod(@Hungry Tiger tiger) {
        assertNotNull(tiger);
        tiger.foo();
        Bean<Tiger> bean = cast(manager.resolve(manager.getBeans(Tiger.class, Hungry.Literal.INSTANCE)));
        assertTrue(bean.getQualifiers().contains(Hungry.Literal.INSTANCE));
    }

    @Test
    public void testSyntheticDecorator(FireTruck truck) {
        assertTrue(truck.decorated());
    }
}
