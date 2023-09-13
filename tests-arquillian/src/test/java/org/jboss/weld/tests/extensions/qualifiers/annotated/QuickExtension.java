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
package org.jboss.weld.tests.extensions.qualifiers.annotated;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.enterprise.util.Nonbinding;

import org.jboss.weld.literal.QualifierLiteral;
import org.jboss.weld.util.annotated.ForwardingAnnotatedMethod;
import org.jboss.weld.util.annotated.ForwardingAnnotatedType;
import org.junit.Assert;

public class QuickExtension implements Extension {

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, final BeanManager manager) {
        Assert.assertFalse(manager.isQualifier(Quick.class));
        Assert.assertFalse(manager.isQualifier(Slow.class));
        event.addQualifier(new QuickAnnotatedType(manager.createAnnotatedType(Quick.class)));
        event.addQualifier(manager.createAnnotatedType(Slow.class));
    }

    @SuppressWarnings({ "unchecked", "serial" })
    private static class QuickAnnotatedType extends ForwardingAnnotatedType<Quick> {

        private Set<Annotation> annotations;

        private Set<AnnotatedMethod<? super Quick>> methods;

        private AnnotatedType<Quick> delegate;

        QuickAnnotatedType(AnnotatedType<Quick> annotatedType) {
            delegate = annotatedType;
            annotations = new HashSet<Annotation>(delegate.getAnnotations());
            annotations.add(QualifierLiteral.INSTANCE);

            methods = new HashSet<AnnotatedMethod<? super Quick>>(delegate.getMethods().size());
            for (final AnnotatedMethod<? super Quick> method : delegate.getMethods()) {
                if ("dirty".equals(method.getJavaMember().getName())) {
                    methods.add(new ForwardingAnnotatedMethod<Quick>() {

                        private Set<Annotation> annotations = new HashSet<Annotation>(
                                Collections.singletonList(new AnnotationLiteral<Nonbinding>() {
                                }));

                        @Override
                        protected AnnotatedMethod<Quick> delegate() {
                            return (AnnotatedMethod<Quick>) method;
                        }

                        @Override
                        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                            return (A) (annotationType.equals(Nonbinding.class) ? annotations.iterator().next() : null);
                        }

                        @Override
                        public Set<Annotation> getAnnotations() {
                            return annotations;
                        }

                        @Override
                        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                            return annotationType.equals(Nonbinding.class) ? true : false;
                        }

                    });
                } else {
                    methods.add(method);
                }
            }

        }

        @Override
        public AnnotatedType<Quick> delegate() {
            return delegate;
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return annotations;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(annotationType)) {
                    return (A) annotation;
                }
            }
            return null;
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return getAnnotation(annotationType) != null;
        }

        @Override
        public Set<AnnotatedMethod<? super Quick>> getMethods() {
            return methods;
        }

    }

}
