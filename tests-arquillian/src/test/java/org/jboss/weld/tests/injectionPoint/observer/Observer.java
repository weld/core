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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.EventMetadata;

@Dependent
public class Observer {

    void observeFooEvent(@Observes Foo event, EventMetadata metadata) {
        assertNotNull(event);
        assertNotNull(metadata);
        assertNotNull(metadata.getInjectionPoint());
        assertEquals(Foo.class, metadata.getType());
        // qualifiers
        assertTrue(metadata.getQualifiers().contains(Any.Literal.INSTANCE));

        checkBean(metadata.getInjectionPoint().getBean());

        assertTrue(metadata.getInjectionPoint().getMember() instanceof Field);
        assertEquals(EventDispatcher.class, metadata.getInjectionPoint().getMember().getDeclaringClass());

        assertTrue(metadata.getInjectionPoint().getAnnotated() instanceof AnnotatedField<?>);

        assertFalse(metadata.getInjectionPoint().isTransient());
        assertFalse(metadata.getInjectionPoint().isDelegate());
        event.observe();
    }

    void observeBarEvent(@Observes Bar event, EventMetadata metadata) {
        assertNotNull(event);
        assertNotNull(metadata);
        assertNotNull(metadata.getInjectionPoint());
        assertEquals(Bar.class, metadata.getType());
        // qualifiers
        assertEquals(4, metadata.getQualifiers().size());
        assertTrue(metadata.getQualifiers().contains(Alpha.Literal.INSTANCE));
        assertTrue(metadata.getQualifiers().contains(Bravo.Literal.INSTANCE));
        assertTrue(metadata.getQualifiers().contains(Charlie.Literal.INSTANCE));
        assertTrue(metadata.getQualifiers().contains(Any.Literal.INSTANCE));

        checkBean(metadata.getInjectionPoint().getBean());
        assertTrue(metadata.getInjectionPoint().getMember() instanceof Field);
        assertEquals(EventDispatcher.class, metadata.getInjectionPoint().getMember().getDeclaringClass());

        assertTrue(metadata.getInjectionPoint().getAnnotated() instanceof AnnotatedField<?>);

        assertTrue(metadata.getInjectionPoint().isTransient());
        assertFalse(metadata.getInjectionPoint().isDelegate());
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
