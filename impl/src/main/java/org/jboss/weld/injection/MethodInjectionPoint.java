/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * High-level representation of an injected method. This class does not need to be serializable because it is never injected.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class MethodInjectionPoint<T, X> extends AbstractCallableInjectionPoint<T, X, Method> {

    private final WeldMethod<T, X> method;

    public static <T, X> MethodInjectionPoint<T, X> of(WeldMethod<T, X> method, Bean<?> declaringBean, BeanManagerImpl manager) {
        return new MethodInjectionPoint<T, X>(method, declaringBean, false, manager);
    }

    public static <T, X> MethodInjectionPoint<T, X> ofObserverOrDisposerMethod(WeldMethod<T, X> method, Bean<?> declaringBean, BeanManagerImpl manager) {
        return new MethodInjectionPoint<T, X>(method, declaringBean, true, manager);
    }

    protected MethodInjectionPoint(WeldMethod<T, X> method, Bean<?> declaringBean, boolean observerOrDisposer, BeanManagerImpl manager) {
        super(method, declaringBean, observerOrDisposer, manager);
        this.method = method;
    }

    public T invoke(Object declaringInstance, BeanManagerImpl manager, CreationalContext<?> creationalContext, Class<? extends RuntimeException> exceptionTypeToThrow) {
        try {
            return getAnnotated().invoke(declaringInstance, getParameterValues(getParameterInjectionPoints(), null, null, manager, creationalContext));
        } catch (IllegalArgumentException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (IllegalAccessException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (InvocationTargetException e) {
            rethrowException(e, exceptionTypeToThrow);
        }
        return null;
    }

    public T invokeWithSpecialValue(Object declaringInstance, Class<? extends Annotation> annotatedParameter, Object parameter, BeanManagerImpl manager,
            CreationalContext<?> creationalContext, Class<? extends RuntimeException> exceptionTypeToThrow) {
        try {
            return getAnnotated().invoke(declaringInstance, getParameterValues(getParameterInjectionPoints(), annotatedParameter, parameter, manager, creationalContext));
        } catch (IllegalArgumentException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (IllegalAccessException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (InvocationTargetException e) {
            rethrowException(e, exceptionTypeToThrow);
        }
        return null;
    }

    public T invokeOnInstance(Object declaringInstance, BeanManagerImpl manager, CreationalContext<?> creationalContext, Class<? extends RuntimeException> exceptionTypeToThrow) {
        try {
            return getAnnotated().invokeOnInstance(declaringInstance, getParameterValues(getParameterInjectionPoints(), null, null, manager, creationalContext));
        } catch (IllegalArgumentException e) {
            rethrowException(e);
        } catch (IllegalAccessException e) {
            rethrowException(e);
        } catch (InvocationTargetException e) {
            rethrowException(e);
        } catch (SecurityException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (NoSuchMethodException e) {
            rethrowException(e, exceptionTypeToThrow);
        }
        return null;
    }

    public T invokeOnInstanceWithSpecialValue(Object declaringInstance, Class<? extends Annotation> annotatedParameter, Object parameter, BeanManagerImpl manager,
            CreationalContext<?> creationalContext, Class<? extends RuntimeException> exceptionTypeToThrow) {
        try {
            return getAnnotated().invokeOnInstance(declaringInstance, getParameterValues(getParameterInjectionPoints(), annotatedParameter, parameter, manager, creationalContext));
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

    public void inject(Object declaringInstance, Object value) {
        try {
            getAnnotated().invoke(declaringInstance, value);
        } catch (IllegalArgumentException e) {
            rethrowException(e);
        } catch (IllegalAccessException e) {
            rethrowException(e);
        } catch (InvocationTargetException e) {
            rethrowException(e);
        }
    }

    /**
     * Helper method for getting the current parameter values from a list of annotated parameters.
     *
     * @param parameters The list of annotated parameter to look up
     * @param manager The Bean manager
     * @return The object array of looked up values
     */
    protected Object[] getParameterValues(List<ParameterInjectionPoint<?, X>> parameters, Class<? extends Annotation> specialParam, Object specialVal, BeanManagerImpl manager,
            CreationalContext<?> creationalContext) {
        Object[] parameterValues = new Object[parameters.size()];
        Iterator<ParameterInjectionPoint<?, X>> iterator = parameters.iterator();
        for (int i = 0; i < parameterValues.length; i++) {
            ParameterInjectionPoint<?, ?> param = iterator.next();
            if (specialParam != null && param.getAnnotated().isAnnotationPresent(specialParam)) {
                parameterValues[i] = specialVal;
            } else {
                parameterValues[i] = param.getValueToInject(manager, creationalContext);
            }
        }
        return parameterValues;
    }

    public WeldMethod<T, X> getAnnotated() {
        return method;
    }
}
