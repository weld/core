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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.inject.spi.ProducerFactory;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.bean.ForwardingBeanAttributes;
import org.jboss.weld.util.reflection.Reflections;

public class BeanExtension implements Extension {

    private Bean<Zoo> zooBean;

    void registerBeans(@Observes AfterBeanDiscovery event, BeanManager manager) {
        // create a synthetic class bean
        {
            AnnotatedType<Office> oat = manager.createAnnotatedType(Office.class);
            BeanAttributes<Office> oa = manager.createBeanAttributes(oat);
            InjectionTargetFactory<Office> factory = manager.getInjectionTargetFactory(oat);
            Bean<?> bean = manager.createBean(oa, Office.class, factory);
            event.addBean(bean);
        }
        // create a serializable synthetic class bean
        {
            AnnotatedType<SerializableOffice> oat = manager.createAnnotatedType(SerializableOffice.class);
            BeanAttributes<SerializableOffice> oa = manager.createBeanAttributes(oat);
            InjectionTargetFactory<SerializableOffice> factory = manager.getInjectionTargetFactory(oat);
            Bean<?> bean = manager.createBean(oa, SerializableOffice.class, factory);
            event.addBean(bean);
        }
        // create a synthetic decorator
        {
            AnnotatedType<VehicleDecorator> oat = manager.createAnnotatedType(VehicleDecorator.class);
            BeanAttributes<VehicleDecorator> oa = addDecoratorStereotype(manager.createBeanAttributes(oat));
            InjectionTargetFactory<VehicleDecorator> factory = manager.getInjectionTargetFactory(oat);
            Bean<?> bean = manager.createBean(oa, VehicleDecorator.class, factory);
            assertTrue(bean instanceof Decorator<?>);
            event.addBean(bean);
        }

        assertNotNull(zooBean);

        // create synthetic producer field
        {
            AnnotatedType<Zoo> zoo = manager.createAnnotatedType(Zoo.class);
            assertEquals(1, zoo.getFields().size());
            AnnotatedField<? super Zoo> field = zoo.getFields().iterator().next();
            BeanAttributes<Lion> attributes = Reflections.cast(starveOut(manager.createBeanAttributes(field)));
            ProducerFactory<Zoo> factory = getManagerImpl(manager).getProducerFactory(field, zooBean);
            event.addBean(manager.createBean(attributes, Zoo.class, factory));
        }
        // create synthetic producer method
        {
            AnnotatedType<Zoo> zoo = manager.createAnnotatedType(Zoo.class);
            AnnotatedMethod<? super Zoo> method = null;
            for (AnnotatedMethod<? super Zoo> _method : zoo.getMethods()) {
                if (_method.getBaseType().equals(Tiger.class)) {
                    method = _method;
                }
            }
            assertNotNull(method);
            BeanAttributes<Tiger> attributes = Reflections.cast(starveOut(manager.createBeanAttributes(method)));
            ProducerFactory<Zoo> factory = getManagerImpl(manager).getProducerFactory(method, zooBean);
            event.addBean(manager.createBean(attributes, Zoo.class, factory));
        }
    }

    void observeZooBean(@Observes ProcessManagedBean<Zoo> event) {
        this.zooBean = event.getBean();
    }

    private <T> BeanAttributes<T> starveOut(final BeanAttributes<T> attributes) {
        return new ForwardingBeanAttributes<T>() {

            @Override
            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new HashSet<Annotation>(attributes.getQualifiers());
                qualifiers.add(Hungry.Literal.INSTANCE);
                qualifiers.remove(Default.Literal.INSTANCE);
                return Collections.unmodifiableSet(qualifiers);
            }

            @Override
            protected BeanAttributes<T> attributes() {
                return attributes;
            }
        };
    }

    private <T> BeanAttributes<T> addDecoratorStereotype(final BeanAttributes<T> attributes) {
        return new ForwardingBeanAttributes<T>() {

            @Override
            protected BeanAttributes<T> attributes() {
                return attributes;
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.<Class<? extends Annotation>> singleton(jakarta.decorator.Decorator.class);
            }
        };
    }

    private static BeanManagerImpl getManagerImpl(BeanManager manager) {
        if (manager instanceof BeanManagerProxy) {
            manager = Reflections.<BeanManagerProxy> cast(manager).delegate();
        }
        return cast(manager);
    }
}
