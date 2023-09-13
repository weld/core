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

import jakarta.enterprise.inject.spi.SessionBeanType;

import org.jboss.arquillian.container.weld.embedded.mock.BeanDeploymentArchiveImpl;
import org.jboss.arquillian.container.weld.embedded.mock.FlatDeployment;
import org.jboss.arquillian.container.weld.embedded.mock.TestContainer;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.ejb.spi.SubclassedComponentDescriptor;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.mock.MockEjbServices;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Verifies that Weld produces subclasses of EJB container-provided subclasses and that methods can be invoked on these
 * subclasses.
 *
 * @author Jozef Hartinger
 * @see WELD-1667
 *
 */
public class SubclassedComponentDescriptorTest {

    private BeanManagerImpl manager;
    private TestContainer container;

    @BeforeClass
    public void prepareContainer() {
        final EjbDescriptor<Foo> fooDescriptor = new EjbDescriptorImpl<Foo>(Foo.class, Foo.class, EnhancedFoo.class,
                SessionBeanType.STATEFUL);
        final EjbDescriptor<Bar> barDescriptor = new EjbDescriptorImpl<Bar>(Bar.class, BarLocal.class, EnhancedBar.class,
                SessionBeanType.STATEFUL);
        final EjbDescriptor<Baz> bazDescriptor = new EjbDescriptorImpl<Baz>(Baz.class, Baz.class, EnhancedBaz.class, null);

        final BeanDeploymentArchive bda = new BeanDeploymentArchiveImpl("1",
                Foo.class,
                Bar.class,
                BarLocal.class,
                BarDecorator.class,
                BarInterceptor.class,
                BarInterceptorBinding.class,
                Baz.class) {
            @Override
            public Collection<EjbDescriptor<?>> getEjbs() {
                return ImmutableSet.<EjbDescriptor<?>> of(fooDescriptor, barDescriptor, bazDescriptor);
            }
        };

        final Deployment deployment = new FlatDeployment(bda) {
            @Override
            public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
                return bda;
            }

            @Override
            protected void configureServices(Environment environment) {
                super.configureServices(environment);
                getServices().add(EjbServices.class, new MockEjbServices());
            }

        };

        container = new TestContainer(deployment).startContainer();
        manager = (BeanManagerImpl) container.getBeanManager(bda);
    }

    @AfterClass
    public void cleanup() {
        container.stopContainer();
    }

    @Test
    public void testSubclassedComponentDescriptor() {
        Foo foo = (Foo) manager.createInjectionTarget(manager.getEjbDescriptor(Foo.class.getSimpleName()))
                .produce(manager.createCreationalContext(null));
        Bar bar = (Bar) manager.createInjectionTarget(manager.getEjbDescriptor(Bar.class.getSimpleName()))
                .produce(manager.createCreationalContext(null));
        Baz baz = (Baz) manager.createInjectionTarget(manager.getEjbDescriptor(Baz.class.getSimpleName()))
                .produce(manager.createCreationalContext(null));

        assertEquals(foo.ping(), 1);
        assertEquals(bar.ping(), 3);
        assertEquals(baz.ping(), 1);

        assertTrue(foo instanceof Enhanced);
        assertTrue(Reflections.<Enhanced> cast(foo).check());

        assertTrue(bar instanceof Enhanced);
        assertTrue(Reflections.<Enhanced> cast(bar).check());

        assertTrue(baz instanceof Enhanced);
        assertTrue(Reflections.<Enhanced> cast(baz).check());

        assertNotNull(foo.getManager());
        assertNotNull(bar.getManager());
        assertNotNull(baz.getManager());

        assertEquals(MockEjbServices.getDescriptors().size(), 2);
        assertTrue(MockEjbServices.getDescriptors().contains(manager.getEjbDescriptor(Foo.class.getSimpleName())));
        assertTrue(MockEjbServices.getDescriptors().contains(manager.getEjbDescriptor(Bar.class.getSimpleName())));
    }

    @Test
    public void testInterceptionModelForConstructor() {
        BasicInjectionTarget<?> it = (BasicInjectionTarget<?>) manager
                .createInjectionTarget(manager.getEjbDescriptor(Foo.class.getSimpleName()));
        InterceptionModel model = manager.getInterceptorModelRegistry().get(it.getAnnotated());
        assertNotNull(model);
        assertTrue(model.hasExternalConstructorInterceptors());
    }

    private static class EjbDescriptorImpl<T> implements EjbDescriptor<T>, SubclassedComponentDescriptor<T> {

        private final Class<? extends T> componentSubclass;
        private final Class<T> beanClass;
        private final Class<?> localInterface;
        private final SessionBeanType type;

        public EjbDescriptorImpl(Class<T> beanClass, Class<?> localInterface, Class<? extends T> componentSubclass,
                SessionBeanType type) {
            this.beanClass = beanClass;
            this.localInterface = localInterface;
            this.componentSubclass = componentSubclass;
            this.type = type;
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
            return SessionBeanType.STATELESS.equals(type);
        }

        @Override
        public boolean isSingleton() {
            return SessionBeanType.SINGLETON.equals(type);
        }

        @Override
        public boolean isStateful() {
            return SessionBeanType.STATEFUL.equals(type);
        }

        @Override
        public boolean isMessageDriven() {
            return type == null;
        }

        @Override
        public boolean isPassivationCapable() {
            return SessionBeanType.STATEFUL.equals(type);
        }
    }
}
