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
package org.jboss.weld.tests.injectionPoint.custom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;

public class BarBean implements Bean<Bar> {

    private final Set<InjectionPoint> injectionPoints;

    private final BeanManager beanManager;

    public BarBean(BeanManager beanManager) {
        this.beanManager = beanManager;
        this.injectionPoints = Collections.<InjectionPoint> singleton(new InjectionPointMetadataInjectionPoint(this));
    }

    @Override
    public Bar create(CreationalContext<Bar> creationalContext) {
        InjectionPoint injectionPointMetadata = (InjectionPoint) beanManager.getInjectableReference(injectionPoints.iterator().next(), creationalContext);
        return new Bar(injectionPointMetadata);
    }

    @Override
    public void destroy(Bar instance, CreationalContext<Bar> creationalContext) {
        creationalContext.release();
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> immutableSet(T... items) {
        Set<T> set = new HashSet<T>();
        Collections.addAll(set, items);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Set<Type> getTypes() {
        return this.<Type> immutableSet(Object.class, Bar.class);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return this.<Annotation> immutableSet(Default.Literal.INSTANCE);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public Class<?> getBeanClass() {
        return Bar.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return injectionPoints;
    }

    /**
     * A "synthetic" injection point used to obtain InjectionPoint metadata.
     */
    private static class InjectionPointMetadataInjectionPoint implements InjectionPoint {

        private final Bean<?> bean;

        private InjectionPointMetadataInjectionPoint(Bean<?> bean) {
            this.bean = bean;
        }

        @Override
        public Type getType() {
            return InjectionPoint.class;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return Collections.<Annotation> singleton(Default.Literal.INSTANCE);
        }

        @Override
        public Bean<?> getBean() {
            return bean;
        }

        @Override
        public Member getMember() {
            return null;
        }

        @Override
        public Annotated getAnnotated() {
            // Dummy annotated needed for validation
            return new AnnotatedField<Bar>() {

                @Override
                public boolean isStatic() {
                    return false;
                }

                @Override
                public AnnotatedType<Bar> getDeclaringType() {
                    return null;
                }

                @Override
                public Type getBaseType() {
                    return null;
                }

                @Override
                public Set<Type> getTypeClosure() {
                    return null;
                }

                @Override
                public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                    return null;
                }

                @Override
                public Set<Annotation> getAnnotations() {
                    return Collections.emptySet();
                }

                @Override
                public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                    return false;
                }

                @Override
                public Field getJavaMember() {
                    return null;
                }
            };
        }

        @Override
        public boolean isDelegate() {
            return false;
        }

        @Override
        public boolean isTransient() {
            return false;
        }

    }

}
