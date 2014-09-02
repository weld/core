/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.ejb.subclass;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import org.jboss.arquillian.container.weld.ee.embedded_1_1.mock.BeanDeploymentArchiveImpl;
import org.jboss.arquillian.container.weld.ee.embedded_1_1.mock.TestContainer;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.ejb.spi.SubclassedComponentDescriptor;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.mock.AbstractDeployment;
import org.jboss.weld.mock.MockEjbServices;
import org.jboss.weld.util.reflection.Reflections;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Verifies that Weld produces subclasses of EJB container-provided subclasses and that methods can be invoked on these subclasses.
 *
 * @author Jozef Hartinger
 * @see WELD-1667
 *
 */
public class SubclassedComponentDescriptorTest {

    @Test
    public void testSubclassedComponentDescriptor() {

        final EjbDescriptor<Foo> fooDescriptor = new EjbDescriptorImpl<Foo>(Foo.class, Foo.class, EnhancedFoo.class);
        final EjbDescriptor<Bar> barDescriptor = new EjbDescriptorImpl<Bar>(Bar.class, BarLocal.class, EnhancedBar.class);

        final BeanDeploymentArchive bda = new BeanDeploymentArchiveImpl("1", Foo.class, Bar.class, BarLocal.class, BarDecorator.class, BarInterceptor.class, BarInterceptorBinding.class) {
            @Override
            public Collection<EjbDescriptor<?>> getEjbs() {
                return ImmutableSet.<EjbDescriptor<?>>of(fooDescriptor, barDescriptor);
            }
        };

        final Deployment deployment = new AbstractDeployment(bda) {
            @Override
            public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
                return bda;
            }
            @Override
            protected void configureServices() {
                super.configureServices();
                getServices().add(EjbServices.class, new MockEjbServices());
            }

        };

        final TestContainer container = new TestContainer(deployment).startContainer();
        final BeanManagerImpl manager = (BeanManagerImpl) container.getBeanManager(bda);
        Foo foo = (Foo) manager.createInjectionTarget(manager.getEjbDescriptor(Foo.class.getSimpleName())).produce(manager.createCreationalContext(null));
        Bar bar = (Bar) manager.createInjectionTarget(manager.getEjbDescriptor(Bar.class.getSimpleName())).produce(manager.createCreationalContext(null));

        assertEquals(foo.ping(), 1);
        assertEquals(bar.ping(), 3);

        assertTrue(foo instanceof Enhanced);
        assertTrue(Reflections.<Enhanced>cast(foo).check());

        assertTrue(bar instanceof Enhanced);
        assertTrue(Reflections.<Enhanced>cast(bar).check());

        assertEquals(MockEjbServices.getDescriptors().size(), 1);
        assertEquals(MockEjbServices.getDescriptors().iterator().next().getBeanClass(), Bar.class);

        assertNotNull(foo.getManager());
        assertNotNull(bar.getManager());
    }

    private static class EjbDescriptorImpl<T> implements EjbDescriptor<T>, SubclassedComponentDescriptor<T> {

        private final Class<? extends T> componentSubclass;
        private final Class<T> beanClass;
        private final Class<?> localInterface;

        public EjbDescriptorImpl(Class<T> beanClass, Class<?> localInterface, Class<? extends T> componentSubclass) {
            this.beanClass = beanClass;
            this.localInterface = localInterface;
            this.componentSubclass = componentSubclass;
        }

        @Override
        public Class<? extends T> getComponentSubclass() {
            return componentSubclass;
        }

        @Override
        public Class<T> getBeanClass() {
            return beanClass;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Collection<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces() {
            return Collections.<BusinessInterfaceDescriptor<?>> singleton(new BusinessInterfaceDescriptor() {
                @Override
                public Class getInterface() {
                    return localInterface;
                }
            });
        }

        @Override
        public Collection<BusinessInterfaceDescriptor<?>> getRemoteBusinessInterfaces() {
            return Collections.emptyList();
        }

        @Override
        public String getEjbName() {
            return beanClass.getSimpleName();
        }

        @Override
        public Collection<Method> getRemoveMethods() {
            return Collections.emptyList();
        }

        @Override
        public boolean isStateless() {
            return false;
        }

        @Override
        public boolean isSingleton() {
            return false;
        }

        @Override
        public boolean isStateful() {
            return true;
        }

        @Override
        public boolean isMessageDriven() {
            return false;
        }

        @Override
        public boolean isPassivationCapable() {
            return true;
        }
    }
}
