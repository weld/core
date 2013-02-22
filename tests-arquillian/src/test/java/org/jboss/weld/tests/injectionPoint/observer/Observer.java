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
package org.jboss.weld.tests.injectionPoint.observer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

public class Observer {

    void observeFooEvent(@Observes Foo event, InjectionPoint ip) {
        assertNotNull(event);
        assertNotNull(ip);
        assertEquals(Foo.class, ip.getType());

        checkBean(ip.getBean());

        assertTrue(ip.getMember() instanceof Field);
        assertEquals(EventDispatcher.class, ip.getMember().getDeclaringClass());

        assertTrue(ip.getAnnotated() instanceof AnnotatedField<?>);

        assertFalse(ip.isTransient());
        assertFalse(ip.isDelegate());
        event.observe();
    }

    void observeBarEvent(@Observes Bar event, InjectionPoint ip) {
        assertNotNull(event);
        assertNotNull(ip);
        assertEquals(Bar.class, ip.getType());
        // qualifiers
        assertEquals(3, ip.getQualifiers().size());
        assertTrue(ip.getQualifiers().contains(Alpha.Literal.INSTANCE));
        assertTrue(ip.getQualifiers().contains(Bravo.Literal.INSTANCE));
        assertTrue(ip.getQualifiers().contains(Charlie.Literal.INSTANCE));

        checkBean(ip.getBean());
        assertTrue(ip.getMember() instanceof Field);
        assertEquals(EventDispatcher.class, ip.getMember().getDeclaringClass());

        assertTrue(ip.getAnnotated() instanceof AnnotatedField<?>);

        assertTrue(ip.isTransient());
        assertFalse(ip.isDelegate());
        event.observe();
    }

    private void checkBean(Bean<?> bean) {
        assertNotNull(bean);
        assertEquals(EventDispatcher.class, bean.getBeanClass());
        assertEquals(2, bean.getInjectionPoints().size());
        assertEquals("dispatcher", bean.getName());
        assertEquals(ApplicationScoped.class, bean.getScope());
    }
}
