/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.event;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
@RunWith(Arquillian.class)
public class GenericEventTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addPackage(GenericEventTest.class.getPackage());
    }

    private static boolean ENTRY_EVENT_OBSERVED;
    private static boolean ENTRY_ADDED_EVENT_OBSERVED;

    @Before
    public void initFlags() {
        ENTRY_EVENT_OBSERVED = false;
        ENTRY_ADDED_EVENT_OBSERVED = false;
    }

    @Inject
    private BeanManager beanManager;

    @Inject
    private Event<EntryEvent<String, String>> event;

    @Inject
    private Event<EntryAddedEvent<String, String>> extendedEvent;

    @Test
    public void testFireGenericEventOnManager() {
        beanManager.fireEvent(new EntryEvent<String, String>("key", "value"));

        Assert.assertTrue(ENTRY_EVENT_OBSERVED);
    }

    @Test
    public void testFireGenericEventOnEvent() {
        event.fire(new EntryEvent<String, String>("key", "value"));

        Assert.assertTrue(ENTRY_EVENT_OBSERVED);
    }

    @Test
    public void testFireExtendedGenericEventOnManager() {
        beanManager.fireEvent(new EntryAddedEvent<String, String>("key", "value"));

        Assert.assertTrue(ENTRY_EVENT_OBSERVED);
        Assert.assertTrue(ENTRY_ADDED_EVENT_OBSERVED);
    }

    @Test
    public void testFireExtendedGenericEventOnEvent() {
        extendedEvent.fire(new EntryAddedEvent<String, String>("key", "value"));

        Assert.assertTrue(ENTRY_EVENT_OBSERVED);
        Assert.assertTrue(ENTRY_ADDED_EVENT_OBSERVED);
    }

    static class EntryEvent<K, V> {
        final K key;
        final V value;

        EntryEvent(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    static class EntryAddedEvent<K, V> extends EntryEvent<K, V> {
        EntryAddedEvent(K key, V value) {
            super(key, value);
        }
    }

    static class Observer {
        void observeEntryEvent(@Observes EntryEvent<String, String> event) {
            ENTRY_EVENT_OBSERVED = true;
        }

        void observeEntryAddedEvent(@Observes EntryAddedEvent<String, String> event) {
            ENTRY_ADDED_EVENT_OBSERVED = true;
        }
    }
}
