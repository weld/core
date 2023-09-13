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
package org.jboss.weld.tests.unit.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.CommonBean;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.serialization.BeanIdentifierIndex;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.junit.Test;

public class BeanIdentifierIndexTest {

    @Test(expected = IllegalStateException.class)
    public void testIndexNotBuilt() {
        new BeanIdentifierIndex().getIdentifier(0);
    }

    @Test
    public void testInvalidIndex() {
        BeanIdentifierIndex index = new BeanIdentifierIndex();
        index.build(Collections.<Bean<?>> emptySet());
        try {
            index.getIdentifier(-10);
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
        try {
            index.getIdentifier(0);
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
        try {
            index.getIdentifier(10);
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test
    public void testGetIndex() {
        BeanIdentifierIndex index = new BeanIdentifierIndex();
        index.build(Collections.<Bean<?>> emptySet());
        try {
            index.getIndex(null);
        } catch (IllegalArgumentException e) {
            // Expected
        }
        assertNull(index.getIndex(new StringBeanIdentifier("foo")));
    }

    @Test
    public void testGetHash() {
        Bean<Object> dummy01 = DummyBean.of("1");
        Bean<Object> dummy02 = DummyBean.of("2");
        BeanIdentifierIndex index01 = new BeanIdentifierIndex();
        index01.build(Collections.singleton(dummy01));
        BeanIdentifierIndex index02 = new BeanIdentifierIndex();
        index02.build(Collections.singleton(dummy01));
        BeanIdentifierIndex index03 = new BeanIdentifierIndex();
        Set<Bean<?>> beans = new HashSet<Bean<?>>();
        beans.add(dummy01);
        beans.add(dummy02);
        index03.build(beans);
        assertTrue(index01.getIndexHash() == index02.getIndexHash());
        assertFalse(index01.getIndexHash() == index03.getIndexHash());
    }

    @Test
    public void testIsEmpty() {
        BeanIdentifierIndex index = new BeanIdentifierIndex();
        index.build(Collections.<Bean<?>> emptySet());
        assertTrue(index.isEmpty());
    }

    @Test
    public void testGetDebugInfo() {
        BeanIdentifierIndex index = new BeanIdentifierIndex();
        Set<Bean<?>> beans = new HashSet<Bean<?>>();
        for (int i = 0; i < 3; i++) {
            beans.add(DummyBean.of(i + ".foo"));
        }
        index.build(beans);
        assertEquals("BeanIdentifierIndex [hash=-1733773048, indexed=3] \n     0: 0.foo\n     1: 1.foo\n     2: 2.foo\n",
                index.getDebugInfo());
    }

    private static class DummyBean<T> extends CommonBean<T> {

        static <T> DummyBean<T> of(String id) {
            return new DummyBean<>(null, new StringBeanIdentifier(id));
        }

        protected DummyBean(BeanAttributes<T> attributes, BeanIdentifier identifier) {
            super(attributes, identifier);
        }

        @Override
        public Class<?> getBeanClass() {
            return null;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return null;
        }

        @Override
        public T create(CreationalContext<T> creationalContext) {
            return null;
        }

        @Override
        public void destroy(T instance, CreationalContext<T> creationalContext) {
        }
    }

}
