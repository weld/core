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
package org.jboss.weld.tests.builtinBeans.metadata.passivation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.util.bean.ForwardingBeanAttributes;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test verifies that injected {@link Bean}, {@link Interceptor} and {@link Decorator} objects are passivation capable
 * dependencies.
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class BeanMetadataPassivationCapableDependencyTest {

    @Inject
    private Car car;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(BeanMetadataPassivationCapableDependencyTest.class))
                .intercept(FastInterceptor.class)
                .decorate(VehicleDecorator.class)
                .addPackage(BeanMetadataPassivationCapableDependencyTest.class.getPackage())
                .addClasses(Utils.class, ForwardingBeanAttributes.class)
                .addAsServiceProvider(Extension.class, CarExtension.class, FastInterceptorExtension.class,
                        VehicleDecoratorExtension.class);
    }

    @Test
    public void testDefaultBeanMetadataPassivationCapableDependency() throws Exception {
        Bean<?> bean = car.getBean();
        assertNotNull(bean);
        Bean<?> bean2 = Utils.deserialize(Utils.serialize(bean));
        assertEquals(bean, bean2);
    }

    @Test
    public void testDefaultInterceptorMetadataPassivationCapableDependency() throws Exception {
        FastInterceptor fastInterceptor = car.getInterceptor();
        assertNotNull(fastInterceptor);
        Interceptor<?> interceptor = fastInterceptor.getInterceptor();
        assertNotNull(interceptor);
        Interceptor<?> interceptor2 = Utils.deserialize(Utils.serialize(interceptor));
        assertEquals(interceptor, interceptor2);
    }

    @Test
    public void testInterceptedBeanMetadataPassivationCapableDependency() throws Exception {
        FastInterceptor fastInterceptor = car.getInterceptor();
        assertNotNull(fastInterceptor);
        Bean<?> interceptedBean = fastInterceptor.getInterceptor();
        assertNotNull(interceptedBean);
        Bean<?> interceptedBean2 = Utils.deserialize(Utils.serialize(interceptedBean));
        assertEquals(interceptedBean, interceptedBean2);
    }

    @Test
    public void testDefaultDecoratorMetadataPassivationCapableDependency() throws Exception {
        VehicleDecorator decoratorInstance = car.getDecoratorInstance();
        assertNotNull(decoratorInstance);
        Decorator<?> decorator = decoratorInstance.getDecorator();
        assertNotNull(decorator);
        Decorator<?> decorator2 = Utils.deserialize(Utils.serialize(decorator));
        assertEquals(decorator, decorator2);
    }

    @Test
    public void testDecoratedBeanMetadataPassivationCapableDependency() throws Exception {
        VehicleDecorator decoratorInstance = car.getDecoratorInstance();
        assertNotNull(decoratorInstance);
        Bean<?> decoratedBean = decoratorInstance.getBean();
        assertNotNull(decoratedBean);
        Bean<?> decoratedBean2 = Utils.deserialize(Utils.serialize(decoratedBean));
        assertEquals(decoratedBean, decoratedBean2);
    }
}
