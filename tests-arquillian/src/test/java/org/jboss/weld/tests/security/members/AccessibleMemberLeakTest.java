/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.security.members;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that members that were set accessible by Weld do not leak to the application.
 *
 * @see https://issues.jboss.org/browse/WELD-1249
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class AccessibleMemberLeakTest {

    @Inject
    private BeanManager manager;

    @Inject
    private SimpleExtension extension;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(AccessibleMemberLeakTest.class))
                .decorate(SimpleDecorator.class)
                .addPackage(AccessibleMemberLeakTest.class.getPackage())
                .addAsServiceProvider(Extension.class, SimpleExtension.class);
    }

    @Test
    @InSequence(-1)
    public void init(Instance<SimpleBean> bean, Instance<Integer> integer, Event<String> event, Instance<Float> f) {
        bean.get();
        integer.destroy(integer.get());
        f.get();
        event.fire("foo");
    }

    @Test
    public void testBeanInjectionPointMembers1() {
        Bean<?> bean = manager.resolve(manager.getBeans(SimpleBean.class));
        testInjectionPoints(bean.getInjectionPoints());
        testInjectionPoints(extension.getSimpleBean().getInjectionPoints());
    }

    @Test
    public void testBeanInjectionPointMembers2() {
        Bean<?> bean = manager.resolve(manager.getBeans(Integer.class));
        testInjectionPoints(bean.getInjectionPoints());
        testInjectionPoints(extension.getIntegerBean().getInjectionPoints());
    }

    @Test
    public void testBeanInjectionPointMembers3() {
        List<Decorator<?>> decorators = manager.resolveDecorators(Collections.<Type> singleton(Simple.class));
        assertEquals(1, decorators.size());
        testInjectionPoints(decorators.get(0).getInjectionPoints());
        testInjectionPoints(extension.getSimpleDecorator().getInjectionPoints());
    }

    @Test
    public void testCreateAnnotatedType() {
        AnnotatedType<?> type = manager.createAnnotatedType(SimpleBean.class);
        testAnnotatedType(type);
    }

    @Test
    public void testAnnotatedTypes() {
        testAnnotatedType(extension.getSimpleBeanType1());
        testAnnotatedType(extension.getSimpleBeanType2());
        testAnnotatedType(extension.getSimpleBeanType3());
        testAnnotatedType(extension.getSimpleBeanType4());
        testAnnotatedType(extension.getSimpleDecoratorType1());
        testAnnotatedType(extension.getSimpleDecoratorType2());
        testAnnotatedType(extension.getSimpleDecoratorType3());
    }

    @Test
    public void testInjectionTarget() {
        testInjectionPoints(extension.getSimpleBeanInjectionTarget().getInjectionPoints());
    }

    @Test
    public void testCreateInjectionTarget() {
        AnnotatedType<SimpleBean> type = manager.createAnnotatedType(SimpleBean.class);
        InjectionTarget<?> it = manager.getInjectionTargetFactory(type).createInjectionTarget(null);
        testInjectionPoints(it.getInjectionPoints());
    }

    @Test
    public void testCreateProducer() {
        AnnotatedType<SimpleBean> type = manager.createAnnotatedType(SimpleBean.class);
        AnnotatedMethod<? super SimpleBean> method = null;
        for (AnnotatedMethod<? super SimpleBean> m : type.getMethods()) {
            if (m.getJavaMember().getName().equals("getDouble")) {
                method = m;
                break;
            }
        }
        Producer<?> producer = manager.getProducerFactory(method, null).createProducer(null);
        testInjectionPoints(producer.getInjectionPoints());
    }

    @Test
    public void testProducerFieldMember() {
        testMember(extension.getProducerField().getJavaMember());
    }

    @Test
    public void testProducerMethodMember() {
        testMember(extension.getIntegerMethod1().getJavaMember());
        testMember(extension.getIntegerMethod2().getJavaMember());
    }

    @Test
    public void testDisposerMethodMember() {
        testMember(extension.getDisposerParameter1().getDeclaringCallable().getJavaMember());
        testMember(extension.getDisposerParameter2().getDeclaringCallable().getJavaMember());
    }

    @Test
    public void testProducer() {
        testInjectionPoints(extension.getIntegerBeanProducer().getInjectionPoints());
    }

    @Test
    public void testCreateInjectionPointWithField() {
        AnnotatedType<SimpleBean> type = manager.createAnnotatedType(SimpleBean.class);
        AnnotatedField<?> field = null;
        for (AnnotatedField<?> f : type.getFields()) {
            if (f.getJavaMember().getName().equals("manager")) {
                field = f;
                break;
            }
        }
        assertNotNull(field);
        InjectionPoint ip = manager.createInjectionPoint(field);
        testMember(ip.getMember());
    }

    @Test
    public void testCreateInjectionPointWithConstructor() {
        AnnotatedType<SimpleBean> type = manager.createAnnotatedType(SimpleBean.class);
        AnnotatedConstructor<?> constructor = null;
        for (AnnotatedConstructor<?> c : type.getConstructors()) {
            if (c.isAnnotationPresent(Inject.class)) {
                constructor = c;
                break;
            }
        }
        assertNotNull(constructor);
        InjectionPoint ip = manager.createInjectionPoint(constructor.getParameters().get(0));
        testMember(ip.getMember());
    }

    @Test
    public void testCreateInjectionPointWithMethod() {
        AnnotatedType<SimpleBean> type = manager.createAnnotatedType(SimpleBean.class);
        AnnotatedMethod<?> method = null;
        for (AnnotatedMethod<?> m : type.getMethods()) {
            if (m.getJavaMember().getName().equals("produceInteger")) {
                method = m;
                break;
            }
        }
        assertNotNull(method);
        InjectionPoint ip = manager.createInjectionPoint(method.getParameters().get(1));
        testMember(ip.getMember());
    }

    @Test
    public void testObserverMethodMember() {
        testMember(extension.getObserverMethodMember().getJavaMember());
    }

    @Test
    public void testInjectedMetadataForField(Instance<Foo> instance) {
        testMember(instance.get().getFieldBar().getInjectionPoint().getMember());
    }

    @Test
    public void testInjectedMetadataForConstructor(Instance<Foo> instance) {
        testMember(instance.get().getConstructorBar().getInjectionPoint().getMember());
    }

    @Test
    public void testInjectedMetadataForInitializer(Instance<Foo> instance) {
        testMember(instance.get().getInitializerBar().getInjectionPoint().getMember());
    }

    private void testMember(Member member) {
        assertFalse(((AccessibleObject) member).isAccessible());
    }

    private void testAnnotatedType(AnnotatedType<?> type) {
        for (AnnotatedConstructor<?> member : type.getConstructors()) {
            assertFalse(member.getJavaMember().isAccessible());
        }
        for (AnnotatedMethod<?> member : type.getMethods()) {
            assertFalse(member.getJavaMember().isAccessible());
        }
        for (AnnotatedField<?> member : type.getFields()) {
            assertFalse(member.getJavaMember().isAccessible());
        }
    }

    private void testInjectionPoints(Collection<InjectionPoint> injectionPoints) {
        assertFalse(injectionPoints.isEmpty());
        for (InjectionPoint ip : injectionPoints) {
            assertFalse(((AccessibleObject) ip.getMember()).isAccessible());
        }
    }
}
