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
package org.jboss.weld.tests.serialization.annotated;

import static org.jboss.weld.util.reflection.Reflections.cast;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedType;

public class FooExtension implements Extension {

    public static final String FOO_ID = FooExtension.class.getName() + "." + Foo.class.getName();

    void registerAnotherFoo(@Observes BeforeBeanDiscovery event, BeanManager manager) {
        event.addAnnotatedType(manager.createAnnotatedType(Foo.class), FOO_ID);
    }

    private AnnotatedType<Foo> backedAnnotatedType;
    private AnnotatedType<Foo> unbackedAnnotatedType;

    public void observeFinalAnnotatedTypes(@Observes AfterBeanDiscovery event) {
        backedAnnotatedType = cast(event.getAnnotatedType(Foo.class, null));
        assertTrue(backedAnnotatedType instanceof BackedAnnotatedType<?>);
        unbackedAnnotatedType = cast(event.getAnnotatedType(Foo.class, FOO_ID));
        assertTrue(unbackedAnnotatedType instanceof UnbackedAnnotatedType<?>);
    }

    public AnnotatedType<Foo> getBackedAnnotatedType() {
        return backedAnnotatedType;
    }

    public AnnotatedType<Foo> getUnbackedAnnotatedType() {
        return unbackedAnnotatedType;
    }

}
