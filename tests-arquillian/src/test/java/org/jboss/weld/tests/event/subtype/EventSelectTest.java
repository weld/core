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
package org.jboss.weld.tests.event.subtype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EventSelectTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EventSelectTest.class))
                .addPackage(EventSelectTest.class.getPackage());
    }

    @Inject
    Event<Object> event;

    @Inject
    Event<Bar> barEvent;

    @Inject
    Observers observers;

    @Test
    public void testSelectSubType() {
        // Check that event object types are used when determining observers
        observers.reset();
        event.select(Bar.class).fire(new FooBarImpl());
        assertNotNull(observers.getBar());
        assertNotNull(observers.getFoo());
    }

    @Test
    public void testInjectedSubType() {
        // Check that selected subtypes are used when determining observers
        observers.reset();
        barEvent.fire(new FooBarImpl());
        assertNotNull(observers.getBar());
        assertNotNull(observers.getFoo());
    }

    @SuppressWarnings("serial")
    @Test
    public void testSelectSubtypeWithWildcard() {
        observers.reset();
        Event<Baz<?>> child = event.select(new TypeLiteral<Baz<?>>() {
        });
        Baz<?> baz = new BazImpl();
        child.fire(baz);
        assertNotNull(observers.getBaz());
        assertEquals("ok", observers.getBaz().get());
    }

    static class BazImpl extends Baz<String> {

        @Override
        String get() {
            return "ok";
        }

    }

}
