/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.event.enhanced;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.util.TypeLiteral;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.events.WeldEvent;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class WeldEventTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder().add(ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(WeldEventTest.class))
                .addPackage(WeldEventTest.class.getPackage())).build();
    }

    @Test
    public void testInstanceOf() {
        try (WeldContainer container = new Weld().initialize()) {
            BeanInjectingEvents bean = container.select(BeanInjectingEvents.class).get();
            // assert that Event is instance of WeldEvent
            Assert.assertNotNull(bean.getPlainEvent());
            Assert.assertTrue(bean.getPlainEvent() instanceof WeldEvent);
        }
    }

    @Test
    public void testWeldEventInjectability() {
        try (WeldContainer container = new Weld().initialize()) {
            BeanInjectingEvents bean = container.select(BeanInjectingEvents.class).get();
            Assert.assertNotNull(bean.getEventObject());
            Assert.assertNotNull(bean.getEventFoo());
            Assert.assertNotNull(bean.getEventListObject());
            Assert.assertNotNull(bean.getEventSomeInterface());
        }
    }

    @Test
    public void testEventObservability() {
        // test few basic fire/observe combinations, just to make sure WeldEvent can work in place of Event
        try (WeldContainer container = new Weld().initialize()) {
            BeanInjectingEvents bean = container.select(BeanInjectingEvents.class).get();
            ObservingBean observer = container.select(ObservingBean.class).get();

            // WeldEvent<Foo>
            observer.reset();
            bean.getEventFoo().fire(new Foo());
            Assert.assertTrue(observer.isFooObserved());

            // WeldEvent<List<Object>>
            observer.reset();
            bean.getEventListObject().fire(new ArrayList<>());
            Assert.assertTrue(observer.isListObjectObserved());
            Assert.assertTrue(observer.isListObserved());

            // select and fire from container.event()
            observer.reset();
            container.event().select(Bar.class, Dubious.Literal.INSTANCE).fire(new Bar());
            Assert.assertTrue(observer.isBarObserved());

            // and now with Types
            Type someInterfaceType = SomeInterface.class;
            Type listFooType = new TypeLiteral<List<Foo>>() {
            }.getType();
            Type listWildcardType = new TypeLiteral<List<?>>() {
            }.getType();

            // WeldEvent<Object> -> WeldEvent<SomeInterface> -> fire(SomeOtherBean)
            observer.reset();
            container.event().<SomeInterface> select(someInterfaceType).fire(container.select(SomeOtherBean.class).get());
            Assert.assertTrue(observer.isSomeInterfaceObserved());
            Assert.assertTrue(observer.isSomeOtherBeanObserved());
            Assert.assertFalse(observer.isSomeTypedBeanObserved());

            // WeldEvent<Object> -> WeldEvent<List<?>> -> fire(ArrayList<Object>)
            observer.reset();
            bean.getEventObject().select(listWildcardType).fire(new ArrayList<Object>());
            Assert.assertFalse(observer.isListObjectObserved());
            Assert.assertTrue(observer.isListObserved());

            // WeldEvent<Object> -> WeldEvent<List<Foo>> -> fire(ArrayList<Foo>)
            observer.reset();
            bean.getEventObject().select(listFooType).fire(new ArrayList<Foo>());
            Assert.assertFalse(observer.isListObjectObserved());
            Assert.assertTrue(observer.isListObserved());
        }
    }

    @Test
    public void testEventSelectByType() {
        try (WeldContainer container = new Weld().initialize()) {
            BeanInjectingEvents bean = container.select(BeanInjectingEvents.class).get();

            // prepare some Types
            Type firstType = SomeInterface.class;
            Type secondType = SomeOtherBean.class;
            Type listFooType = new TypeLiteral<List<Foo>>() {
            }.getType();
            Type listWildcardType = new TypeLiteral<List<?>>() {
            }.getType();

            // verify invalid behaviour - selecting from Event<X> where X != Object
            try {
                // using Type from SomeInterface -> SomeOtherBean
                bean.getEventSomeInterface().select(secondType);
                Assert.fail();
            } catch (IllegalStateException e) {
                // expected
            }

            try {
                // using Type from List<Object> -> List<?>
                bean.getEventListObject().select(listWildcardType);
                Assert.fail();
            } catch (IllegalStateException e) {
                // expected
            }

            // verify valid behaviour, following selections should work
            bean.getEventObject().select(firstType);
            bean.getEventObject().select(secondType);
            bean.getEventObject().select(listFooType);
            bean.getEventObject().select(listWildcardType);
        }
    }

    @Test
    public void testSelectByTypeAndOtherSelect() {
        // just to make sure you can select by Type and get back WeldInstance which allows you to keep selecting by "old" means
        try (WeldContainer container = new Weld().initialize()) {
            Type type = new TypeLiteral<List<Foo>>() {
            }.getType();
            container.event().select(type).select(new TypeLiteral<ArrayList<Foo>>() {
            }, Dubious.Literal.INSTANCE);
        }
    }
}
