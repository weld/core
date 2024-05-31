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
package org.jboss.weld.injection;

import static org.jboss.weld.injection.Exceptions.rethrowException;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.TransientReference;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.reflection.Reflections;

/**
 * {@link MethodInjectionPoint} that delegates to a static method.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 * @param <X>
 */
class StaticMethodInjectionPoint<T, X> extends MethodInjectionPoint<T, X> {

    private final int specialInjectionPointIndex;
    private final AnnotatedMethod<X> annotatedMethod;
    final Method accessibleMethod;

    StaticMethodInjectionPoint(MethodInjectionPointType methodInjectionPointType, EnhancedAnnotatedMethod<T, X> enhancedMethod,
            Bean<?> declaringBean, Class<?> declaringComponentClass,
            Set<Class<? extends Annotation>> specialParameterMarkers, InjectionPointFactory factory, BeanManagerImpl manager) {
        super(methodInjectionPointType, enhancedMethod, declaringBean, declaringComponentClass, factory, manager);
        this.accessibleMethod = Reflections.getAccessibleCopyOfMember(enhancedMethod.getJavaMember());
        this.annotatedMethod = enhancedMethod.slim();
        this.specialInjectionPointIndex = initSpecialInjectionPointIndex(enhancedMethod, specialParameterMarkers);
    }

    private static <X> int initSpecialInjectionPointIndex(EnhancedAnnotatedMethod<?, X> enhancedMethod,
            Set<Class<? extends Annotation>> specialParameterMarkers) {
        if (specialParameterMarkers == null || specialParameterMarkers.isEmpty()) {
            return -1;
        }
        List<EnhancedAnnotatedParameter<?, X>> parameters = Collections.emptyList();
        for (Class<? extends Annotation> marker : specialParameterMarkers) {
            parameters = enhancedMethod.getEnhancedParameters(marker);
            if (!parameters.isEmpty()) {
                break;
            }
        }
        if (parameters.isEmpty()) {
            throw new org.jboss.weld.exceptions.IllegalArgumentException(
                    "Not a disposer nor observer method: " + enhancedMethod);
        }
        return parameters.get(0).getPosition();
    }

    public T invoke(Object receiver, Object specialValue, BeanManagerImpl manager, CreationalContext<?> ctx,
            Class<? extends RuntimeException> exceptionTypeToThrow) {
        CreationalContext<?> transientReferenceContext = null;
        if (hasTransientReferenceParameter) {
            transientReferenceContext = manager.createCreationalContext(null);
        }
        try {
            return invoke(receiver, getParameterValues(specialValue, manager, ctx, transientReferenceContext),
                    exceptionTypeToThrow);
        } finally {
            if (hasTransientReferenceParameter) {
                transientReferenceContext.release();
            }
        }
    }

    public T invoke(Object receiver, Object[] parameters, Class<? extends RuntimeException> exceptionTypeToThrow) {
        try {
            return cast(getMethod(receiver).invoke(receiver, parameters));
        } catch (IllegalArgumentException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (SecurityException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (IllegalAccessException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (InvocationTargetException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (NoSuchMethodException e) {
            rethrowException(e, exceptionTypeToThrow);
        }
        return null;
    }

    /**
     * Helper method for getting the current parameter values from a list of annotated parameters.
     *
     * @param parameters The list of annotated parameter to look up
     * @param manager The Bean manager
     * @return The object array of looked up values
     */
    protected Object[] getParameterValues(Object specialVal, BeanManagerImpl manager, CreationalContext<?> ctx,
            CreationalContext<?> transientReferenceContext) {
        if (getInjectionPoints().isEmpty()) {
            if (specialInjectionPointIndex == -1) {
                return Arrays2.EMPTY_ARRAY;
            } else {
                return new Object[] { specialVal };
            }
        }
        Object[] parameterValues = new Object[getParameterInjectionPoints().size()];
        List<ParameterInjectionPoint<?, X>> parameters = getParameterInjectionPoints();
        for (int i = 0; i < parameterValues.length; i++) {
            ParameterInjectionPoint<?, ?> param = parameters.get(i);
            if (i == specialInjectionPointIndex) {
                parameterValues[i] = specialVal;
            } else if (hasTransientReferenceParameter && param.getAnnotated().isAnnotationPresent(TransientReference.class)) {
                parameterValues[i] = param.getValueToInject(manager, transientReferenceContext);
            } else {
                parameterValues[i] = param.getValueToInject(manager, ctx);
            }
        }
        return parameterValues;
    }

    protected Method getMethod(Object receiver) throws NoSuchMethodException {
        return accessibleMethod;
    }

    @Override
    public AnnotatedMethod<X> getAnnotated() {
        return annotatedMethod;
    }
}
