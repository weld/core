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
package org.jboss.weld.tests.repeatable;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.repeatable.RepeatableQualifier.Literal;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EventWithRepeatableQualifierTest {

    private static final String EVENT = "event";

    @Inject
    private Observer observer;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EventWithRepeatableQualifierTest.class)).addPackage(EventWithRepeatableQualifierTest.class.getPackage());
    }

    @Test
    public void testWithBeanManager(BeanManager manager) {
        observer.reset();
        manager.getEvent().select(String.class, new Literal("foo"), new Literal("bar")).fire(EVENT);
        Assert.assertTrue(observer.getAll().contains(EVENT));
        Assert.assertTrue(observer.getFoo().contains(EVENT));
        Assert.assertTrue(observer.getFooBar().contains(EVENT));
        Assert.assertFalse(observer.getFooBarBaz().contains(EVENT));
        Assert.assertFalse(observer.getFooQux().contains(EVENT));
        observer.reset();
        manager.getEvent().select(String.class, new Literal("foo"), new Literal("bar"), new Literal("baz")).fire(EVENT);
        Assert.assertTrue(observer.getAll().contains(EVENT));
        Assert.assertTrue(observer.getFoo().contains(EVENT));
        Assert.assertTrue(observer.getFooBar().contains(EVENT));
        Assert.assertTrue(observer.getFooBarBaz().contains(EVENT));
        Assert.assertFalse(observer.getFooQux().contains(EVENT));
    }

    @Test
    public void testWithEvent(Event<String> event) {
        observer.reset();
        event.select(new Literal("foo"), new Literal("bar")).fire(EVENT);
        Assert.assertTrue(observer.getAll().contains(EVENT));
        Assert.assertTrue(observer.getFoo().contains(EVENT));
        Assert.assertTrue(observer.getFooBar().contains(EVENT));
        Assert.assertFalse(observer.getFooBarBaz().contains(EVENT));
        Assert.assertFalse(observer.getFooQux().contains(EVENT));
        observer.reset();
        event.select(new Literal("foo"), new Literal("bar"), new Literal("baz")).fire(EVENT);
        Assert.assertTrue(observer.getAll().contains(EVENT));
        Assert.assertTrue(observer.getFoo().contains(EVENT));
        Assert.assertTrue(observer.getFooBar().contains(EVENT));
        Assert.assertTrue(observer.getFooBarBaz().contains(EVENT));
        Assert.assertFalse(observer.getFooQux().contains(EVENT));
    }


}
