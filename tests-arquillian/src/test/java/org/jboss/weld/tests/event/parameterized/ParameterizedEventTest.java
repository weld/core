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
package org.jboss.weld.tests.event.parameterized;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test that verifies that the container uses the runtime type of the event object as the event type. If the event type contains
 * an unresolved type variable the selected type is used to resolve it.
 *
 * @author Jozef Hartinger
 *
 * @see WELD-1272
 * @see CDI-256
 */
@RunWith(Arquillian.class)
public class ParameterizedEventTest {

    @Inject
    private Event<Object> event;

    @Inject
    private Event<Foo<List<Integer>>> integerListFooEvent;

    @Inject
    private Event<Bar<List<Integer>>> integerListBarEvent;

    @Inject
    private EventObserver observer;

    @Inject
    private IntegerObserver integerObserver;

    @Inject
    private StringObserver stringObserver;

    @Inject
    Event<Foo<? extends Number>> fooEvent;

    @Inject
    Event<Foo<?>> fooEventUnbound;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ParameterizedEventTest.class))
                .addPackage(ParameterizedEventTest.class.getPackage());
    }

    @Test
    public void testSelectedEventTypeUsedForResolvingEventTypeArguments() {
        reset();
        integerListBarEvent.fire(new Bar<List<Integer>>());

        assertTrue(observer.isIntegerListFooableObserved());
        assertTrue(observer.isIntegerListFooObserved());
        assertTrue(observer.isIntegerListBarObserved());
        assertFalse(observer.isBazObserved());
        assertFalse(observer.isStringListFooableObserved());

        assertTrue(integerObserver.isFooableObserved());
        assertTrue(integerObserver.isFooObserved());
        assertTrue(integerObserver.isBarObserved());

        assertFalse(stringObserver.isFooableObserved());
        assertFalse(stringObserver.isFooObserved());
        assertFalse(stringObserver.isBarObserved());
    }

    @Test
    public void testSelectedEventTypeUsedForResolvingEventTypeArguments2() {
        reset();
        @SuppressWarnings("serial")
        Event<Foo<List<Integer>>> selectedEvent = event.select(new TypeLiteral<Foo<List<Integer>>>() {
        });
        selectedEvent.fire(new Foo<List<Integer>>());

        assertTrue(observer.isIntegerListFooableObserved());
        assertTrue(observer.isIntegerListFooObserved());
        assertFalse(observer.isIntegerListBarObserved());
        assertFalse(observer.isBazObserved());
        assertFalse(observer.isStringListFooableObserved());

        assertTrue(integerObserver.isFooableObserved());
        assertTrue(integerObserver.isFooObserved());
        assertFalse(integerObserver.isBarObserved());

        assertFalse(stringObserver.isFooableObserved());
        assertFalse(stringObserver.isFooObserved());
        assertFalse(stringObserver.isBarObserved());
    }

    @Test
    public void testSelectedEventTypeCombinedWithEventObjectRuntimeTypeForResolvingEventTypeArguments() {
        reset();
        @SuppressWarnings("serial")
        Event<Foo<List<Integer>>> selectedEvent = event.select(new TypeLiteral<Foo<List<Integer>>>() {
        });
        selectedEvent.fire(new Bar<List<Integer>>());

        assertTrue(observer.isIntegerListFooableObserved());
        assertTrue(observer.isIntegerListFooObserved());
        assertTrue(observer.isIntegerListBarObserved());
        assertFalse(observer.isBazObserved());
        assertFalse(observer.isStringListFooableObserved());

        assertTrue(integerObserver.isFooableObserved());
        assertTrue(integerObserver.isFooObserved());
        assertTrue(integerObserver.isBarObserved());

        assertFalse(stringObserver.isFooableObserved());
        assertFalse(stringObserver.isFooObserved());
        assertFalse(stringObserver.isBarObserved());
    }

    @Test
    public void testSelectedEventTypeCombinedWithEventObjectRuntimeTypeForResolvingEventTypeArguments2() {
        reset();
        @SuppressWarnings("serial")
        Event<List<Character>> selectedEvent = event.select(new TypeLiteral<List<Character>>() {
        });
        selectedEvent.fire(new ArrayList<Character>());

        assertTrue(observer.isCharacterListObserved());
    }

    @Test
    public void testEventObjectTypeUsed() {
        reset();
        integerListBarEvent.fire(new Baz());

        assertTrue(observer.isIntegerListFooableObserved());
        assertTrue(observer.isIntegerListFooObserved());
        assertTrue(observer.isIntegerListBarObserved());
        assertTrue(observer.isBazObserved());
        assertFalse(observer.isStringListFooableObserved());

        assertTrue(integerObserver.isFooableObserved());
        assertTrue(integerObserver.isFooObserved());
        assertTrue(integerObserver.isBarObserved());

        assertFalse(stringObserver.isFooableObserved());
        assertFalse(stringObserver.isFooObserved());
        assertFalse(stringObserver.isBarObserved());
    }

    @Test
    public void testUnresolvedTypeVariableDetected1() {
        try {
            integerListFooEvent.fire(new Blah<List<Integer>, Integer>());
            Assert.fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    @SuppressWarnings("serial")
    public <T> void testUnresolvedTypeVariableDetected2() {
        try {
            event.select(new TypeLiteral<Map<Exception, T>>() {
            }).fire(new HashMap<Exception, T>());
            Assert.fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    @SuppressWarnings("serial")
    public <T> void testUnresolvedTypeVariableDetected3() {
        try {
            event.select(new TypeLiteral<ArrayList<List<List<List<T>>>>>() {
            }).fire(new ArrayList<List<List<List<T>>>>());
            Assert.fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testWildcardIsResolvable() {
        reset();
        fooEvent.fire(new Bar<Integer>());
        assertTrue(observer.isIntegerFooObserved());
    }

    @Test
    public <T extends Number> void testWildcardIsResolvable2() {
        reset();
        fooEvent.fire(new Bar<T>());
        assertTrue(observer.isIntegerFooObserved());
    }

    @Test
    public <T> void testWildcardIsResolvable3() {
        reset();
        fooEventUnbound.fire(new Bar<T>());
        assertFalse(observer.isIntegerFooObserved());
    }

    private void reset() {
        observer.reset();
        integerObserver.reset();
        stringObserver.reset();
    }
}
