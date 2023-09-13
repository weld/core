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
package org.jboss.weld.tests.beanManager.producer;

import static org.jboss.weld.util.reflection.Reflections.cast;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SyntheticProducerTest {

    @Inject
    private BeanManagerImpl manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SyntheticProducerTest.class))
                .addPackage(SyntheticProducerTest.class.getPackage());
    }

    private <X, A extends AnnotatedMember<? super X>> A getAnnotatedMember(Class<X> javaClass, String memberName) {
        AnnotatedType<X> type = manager.createAnnotatedType(javaClass);
        for (AnnotatedField<? super X> field : type.getFields()) {
            if (field.getJavaMember().getName().equals(memberName)) {
                return cast(field);
            }
        }
        for (AnnotatedMethod<? super X> method : type.getMethods()) {
            if (method.getJavaMember().getName().equals(memberName)) {
                return cast(method);
            }
        }
        throw new IllegalArgumentException("Member " + memberName + " not found on " + javaClass);
    }

    @Test
    public void testStaticProducerField() {
        AnnotatedField<? super Factory> field = this.<Factory, AnnotatedField<Factory>> getAnnotatedMember(Factory.class,
                "WOODY");
        Producer<Toy> producer = cast(manager.getProducerFactory(field, null).createProducer(null));
        assertNotNull(producer);
        assertTrue(producer.getInjectionPoints().isEmpty());
        Toy woody = producer.produce(manager.<Toy> createCreationalContext(null));
        assertEquals("Woody", woody.getName());
    }

    @Test
    public void testNonStaticProducerField() {
        AnnotatedField<? super AnotherFactory> field = this
                .<AnotherFactory, AnnotatedField<AnotherFactory>> getAnnotatedMember(AnotherFactory.class, "jessie");
        Bean<AnotherFactory> declaringBean = cast(manager.resolve(manager.getBeans(AnotherFactory.class)));
        Producer<Toy> producer = cast(manager.getProducerFactory(field, declaringBean).createProducer(null));
        assertNotNull(producer);
        assertTrue(producer.getInjectionPoints().isEmpty());
        Toy jessie = producer.produce(manager.<Toy> createCreationalContext(null));
        assertEquals("Jessie", jessie.getName());
    }

    @Test
    public void testStaticProducerMethod() {
        AnnotatedMethod<? super Factory> method = this.<Factory, AnnotatedMethod<Factory>> getAnnotatedMember(Factory.class,
                "getBuzz");
        Producer<Toy> producer = cast(manager.getProducerFactory(method, null).createProducer(null));
        assertNotNull(producer);
        validateInjectionPoints(producer.getInjectionPoints());
        Toy buzz = producer.produce(manager.<Toy> createCreationalContext(null));
        assertEquals("Buzz Lightyear", buzz.getName());
    }

    @Test
    public void testNonStaticProducerMethod() {
        AnnotatedMethod<? super AnotherFactory> method = this
                .<AnotherFactory, AnnotatedMethod<AnotherFactory>> getAnnotatedMember(AnotherFactory.class, "getRex");
        Bean<AnotherFactory> declaringBean = cast(manager.resolve(manager.getBeans(AnotherFactory.class)));
        Producer<Toy> producer = cast(manager.getProducerFactory(method, declaringBean).createProducer(null));
        assertNotNull(producer);
        validateInjectionPoints(producer.getInjectionPoints());
        Toy rex = producer.produce(manager.<Toy> createCreationalContext(null));
        assertEquals("Rex", rex.getName());
    }

    private void validateInjectionPoints(Set<InjectionPoint> injectionPoints) {
        assertEquals(2, injectionPoints.size());
        for (InjectionPoint ip : injectionPoints) {
            AnnotatedParameter<Factory> parameter = Reflections.<AnnotatedParameter<Factory>> cast(ip.getAnnotated());
            if (parameter.getPosition() == 0) {
                assertEquals(BeanManager.class, parameter.getBaseType());
            } else if (parameter.getPosition() == 1) {
                assertEquals(SpaceSuit.class, Reflections.getRawType(parameter.getBaseType()));
            } else {
                Assert.fail("Unexpected injection point " + ip);
            }
            assertFalse(ip.isDelegate());
            assertFalse(ip.isTransient());
            assertNull(ip.getBean());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProducerMethod1() {
        AnnotatedMethod<? super Factory> method = this.<Factory, AnnotatedMethod<Factory>> getAnnotatedMember(Factory.class,
                "invalidProducerMethod1");
        manager.getProducerFactory(method, null).createProducer(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProducerMethod2() {
        // method is not static but no declaringBean is provided
        AnnotatedMethod<? super Factory> method = this.<Factory, AnnotatedMethod<Factory>> getAnnotatedMember(Factory.class,
                "invalidProducerMethod2");
        manager.getProducerFactory(method, null).createProducer(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProducerField1() {
        AnnotatedField<? super Factory> field = this.<Factory, AnnotatedField<Factory>> getAnnotatedMember(Factory.class,
                "INVALID_FIELD1");
        manager.getProducerFactory(field, null).createProducer(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProducerField2() {
        AnnotatedField<? super Factory> field = this.<Factory, AnnotatedField<Factory>> getAnnotatedMember(Factory.class,
                "INVALID_FIELD2");
        manager.getProducerFactory(field, null).createProducer(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProducerField3() {
        // field is not static but no declaringBean is provided
        AnnotatedField<? super Factory> field = this.<Factory, AnnotatedField<Factory>> getAnnotatedMember(Factory.class,
                "INVALID_FIELD3");
        manager.getProducerFactory(field, null).createProducer(null);
    }
}
