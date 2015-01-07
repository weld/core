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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.CommonBean;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.serialization.BeanIdentifierIndex;
import org.junit.Test;

public class BeanIdentifierIndexTest {

    @Test(expected=IllegalStateException.class)
    public void testIndexNotBuilt() {
        new BeanIdentifierIndex().getIdentifier(0);
    }

    @Test
    public void testInvalidIndex() {
        BeanIdentifierIndex index = new BeanIdentifierIndex();
        index.build(Collections.<Bean<?>>emptySet());
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
        index.build(Collections.<Bean<?>>emptySet());
        try {
            index.getIndex(null);
        } catch (IllegalArgumentException e) {
            // Expected
        }
        assertNull(index.getIndex(new StringBeanIdentifier("foo")));
    }

    @Test
    public void testGetHash() {
        Bean<Object> dummy01 = new CommonBean<Object>(null, new StringBeanIdentifier("1")) {
            @Override
            public Class<?> getBeanClass() {
                return null;
            }
            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return null;
            }
            @Override
            public Object create(CreationalContext<Object> creationalContext) {
                return null;
            }
            @Override
            public void destroy(Object instance, CreationalContext<Object> creationalContext) {
            }
        };
        Bean<Object> dummy02 = new CommonBean<Object>(null, new StringBeanIdentifier("2")) {
            @Override
            public Class<?> getBeanClass() {
                return null;
            }
            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return null;
            }
            @Override
            public Object create(CreationalContext<Object> creationalContext) {
                return null;
            }
            @Override
            public void destroy(Object instance, CreationalContext<Object> creationalContext) {
            }
        };
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

}
