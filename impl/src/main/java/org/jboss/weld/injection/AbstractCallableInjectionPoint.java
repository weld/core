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
package org.jboss.weld.injection;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.TransientReference;
import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedCallable;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.injection.attributes.SpecialParameterInjectionPoint;
import org.jboss.weld.injection.attributes.WeldInjectionPointAttributes;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.InjectionPoints;
import org.jboss.weld.util.collections.ListToSet;

abstract class AbstractCallableInjectionPoint<T, X, S extends Member> implements WeldInjectionPointAttributes<T, S> {

    private final Bean<?> declaringBean;
    private final List<ParameterInjectionPoint<?, X>> parameters;
    private final Set<InjectionPoint> injectionPoints;
    protected final boolean hasTransientReferenceParameter;

    protected AbstractCallableInjectionPoint(EnhancedAnnotatedCallable<T, X, S> callable, Bean<?> declaringBean,
            Class<?> declaringComponentClass, boolean observerOrDisposer, InjectionPointFactory factory,
            BeanManagerImpl manager) {
        this.declaringBean = declaringBean;
        this.parameters = factory.getParameterInjectionPoints(callable, declaringBean, declaringComponentClass, manager,
                observerOrDisposer);
        if (observerOrDisposer) {
            this.injectionPoints = InjectionPoints.filterOutSpecialParameterInjectionPoints(parameters);
        } else {
            this.injectionPoints = new ListToSet<InjectionPoint>() {
                @Override
                protected List<InjectionPoint> delegate() {
                    return cast(getParameterInjectionPoints());
                }
            };
        }
        this.hasTransientReferenceParameter = initHasTransientReference(callable.getEnhancedParameters());
    }

    private static boolean initHasTransientReference(List<? extends EnhancedAnnotatedParameter<?, ?>> parameters) {
        for (EnhancedAnnotatedParameter<?, ?> parameter : parameters) {
            if (parameter.isAnnotationPresent(TransientReference.class)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Type getType() {
        return getAnnotated().getBaseType();
    }

    @Override
    public Set<Annotation> getQualifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bean<?> getBean() {
        return declaringBean;
    }

    @Override
    public boolean isDelegate() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public <A extends Annotation> A getQualifier(Class<A> annotationType) {
        A qualifier = getAnnotated().getAnnotation(annotationType);
        if (getQualifiers().contains(qualifier)) {
            return qualifier;
        } else {
            return null;
        }
    }

    @Override
    public Member getMember() {
        return getAnnotated().getJavaMember();
    }

    @Override
    public abstract AnnotatedCallable<X> getAnnotated();

    public List<ParameterInjectionPoint<?, X>> getParameterInjectionPoints() {
        return parameters;
    }

    /**
     * Returns a set of {@link InjectionPoint} instances of this constructor/method. This set never contains a
     * {@link SpecialParameterInjectionPoint} and is therefore suitable for use outside of Weld. The returned set
     * is immutable.
     */
    public Set<InjectionPoint> getInjectionPoints() {
        return injectionPoints;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractCallableInjectionPoint<?, ?, ?>) {
            AbstractCallableInjectionPoint<?, ?, ?> ip = (AbstractCallableInjectionPoint<?, ?, ?>) obj;
            if (AnnotatedTypes.compareAnnotatedCallable(getAnnotated(), ip.getAnnotated())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getAnnotated().hashCode();
    }

    @Override
    public String toString() {
        return getAnnotated().toString();
    }
}
