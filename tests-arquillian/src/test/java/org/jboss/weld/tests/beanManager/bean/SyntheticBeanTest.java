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
package org.jboss.weld.tests.beanManager.bean;

import static org.jboss.weld.util.reflection.Reflections.cast;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
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

    @Inject
    FireTruck truck;

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SyntheticBeanTest.class))
                .intercept(SimpleInterceptor.class, LifecycleInterceptor.class).decorate(VehicleDecorator.class)
                .addPackage(Simple.class.getPackage())
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
        Bean<Office> bean = cast(manager.resolve(manager.getBeans(SerializableOffice.class, Any.Literal.INSTANCE)));
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
