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
package org.jboss.weld.tests.specialization.extension.broken;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Named;

import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.util.annotated.ForwardingAnnotatedType;

public class ModifyingExtension implements Extension {

    public void registerAdditionalFooAnnotatedType(@Observes BeforeBeanDiscovery event, BeanManager manager) {
        final AnnotatedType<Foo> anotherFoo = manager.createAnnotatedType(Foo.class);
        AnnotatedType<Foo> modifiedAnotherFoo = new ForwardingAnnotatedType<Foo>() {

            private final NamedLiteral qualifierInstance = new NamedLiteral("anotherFoo");

            @Override
            public AnnotatedType<Foo> delegate() {
                return anotherFoo;
            }

            @Override
            public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                if (Named.class.equals(annotationType)) {
                    return cast(qualifierInstance);
                }
                return null;
            }

            @Override
            public Set<Annotation> getAnnotations() {
                return Collections.<Annotation>singleton(qualifierInstance);
            }

            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                return Named.class.equals(annotationType);
            }
        };
        event.addAnnotatedType(modifiedAnotherFoo, Foo.class.getSimpleName() + "Modified");
    }
}
