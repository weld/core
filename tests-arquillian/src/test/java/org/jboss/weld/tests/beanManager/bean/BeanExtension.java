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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.util.bean.ForwardingBeanAttributes;
import org.jboss.weld.util.reflection.Reflections;

public class BeanExtension implements Extension {

    private Bean<Lion> hungryLion;
    private Bean<Lion> hungryTiger;

    void registerBeans(@Observes AfterBeanDiscovery event, BeanManager manager) {
        // create a synthetic class bean
        {
            AnnotatedType<Office> oat = manager.createAnnotatedType(Office.class);
            BeanAttributes<Office> oa = manager.createBeanAttributes(oat);
            InjectionTarget<Office> oit = manager.createInjectionTarget(oat);
            Bean<?> bean = manager.createBean(oa, Office.class, oit);
            event.addBean(bean);
        }
        // create a serializable synthetic class bean
        {
            AnnotatedType<SerializableOffice> oat = manager.createAnnotatedType(SerializableOffice.class);
            BeanAttributes<SerializableOffice> oa = manager.createBeanAttributes(oat);
            InjectionTarget<SerializableOffice> oit = manager.createInjectionTarget(oat);
            Bean<?> bean = manager.createBean(oa, SerializableOffice.class, oit);
            event.addBean(bean);
        }
        // create a synthetic decorator
        {
            AnnotatedType<VehicleDecorator> oat = manager.createAnnotatedType(VehicleDecorator.class);
            BeanAttributes<VehicleDecorator> oa = addDecoratorStereotype(manager.createBeanAttributes(oat));
            InjectionTarget<VehicleDecorator> oit = manager.createInjectionTarget(oat);
            Bean<?> bean = manager.createBean(oa, VehicleDecorator.class, oit);
            assertTrue(bean instanceof Decorator<?>);
            event.addBean(bean);
        }

        // synthetic producer field
        assertNotNull(hungryLion);
        event.addBean(hungryLion);
        // synthetic producer method
        assertNotNull(hungryTiger);
        event.addBean(hungryTiger);
    }

    void prepareHungryLion(@Observes ProcessProducer<Zoo, Lion> event, BeanManager manager) {
        AnnotatedType<Zoo> zoo = manager.createAnnotatedType(Zoo.class);
        assertEquals(1, zoo.getFields().size());
        BeanAttributes<Lion> attributes = Reflections.cast(starveOut(manager.createBeanAttributes(zoo.getFields().iterator().next())));
        Producer<Lion> producer = event.getProducer();
        hungryLion = Reflections.cast(manager.createBean(attributes, Zoo.class, producer));
    }

    void prepareHungryTiger(@Observes ProcessProducer<Zoo, Tiger> event, BeanManager manager) {
        AnnotatedType<Zoo> zoo = manager.createAnnotatedType(Zoo.class);
        AnnotatedMethod<?> method = null;
        for (AnnotatedMethod<?> _method : zoo.getMethods()) {
            if (_method.getBaseType().equals(Tiger.class)) {
                method = _method;
            }
        }
        assertNotNull(method);
        BeanAttributes<Tiger> attributes = Reflections.cast(starveOut(manager.createBeanAttributes(method)));
        Producer<Tiger> producer = event.getProducer();
        hungryTiger = Reflections.cast(manager.createBean(attributes, Zoo.class, producer));
    }

    private <T> BeanAttributes<T> starveOut(final BeanAttributes<T> attributes) {
        return new ForwardingBeanAttributes<T>() {

            @Override
            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new HashSet<Annotation>(attributes.getQualifiers());
                qualifiers.add(Hungry.Literal.INSTANCE);
                qualifiers.remove(DefaultLiteral.INSTANCE);
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
                return Collections.<Class<? extends Annotation>> singleton(javax.decorator.Decorator.class);
            }
        };
    }
}
