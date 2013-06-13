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
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.TransientReference;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.security.GetAccessibleCopyOfMember;
import org.jboss.weld.security.MethodLookupAction;

import com.google.common.collect.ImmutableMap;

/**
 * High-level representation of an injected method. This class does not need to be serializable because it is never injected.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class MethodInjectionPoint<T, X> extends AbstractCallableInjectionPoint<T, X, Method> {

    private final AnnotatedMethod<X> annotatedMethod;
    private final Method accessibleMethod;

    private volatile Map<Class<?>, Method> methods;

    protected MethodInjectionPoint(EnhancedAnnotatedMethod<T, X> enhancedMethod, Bean<?> declaringBean, Class<?> declaringComponentClass, boolean observerOrDisposer, InjectionPointFactory factory, BeanManagerImpl manager) {
        super(enhancedMethod, declaringBean, declaringComponentClass, observerOrDisposer, factory, manager);
        this.annotatedMethod = enhancedMethod.slim();
        this.accessibleMethod = AccessController.doPrivileged(new GetAccessibleCopyOfMember<Method>(annotatedMethod.getJavaMember()));
        this.methods = Collections.<Class<?>, Method>singletonMap(annotatedMethod.getJavaMember().getDeclaringClass(), accessibleMethod);
    }

    public T invoke(Object declaringInstance, BeanManagerImpl manager, CreationalContext<?> creationalContext, Class<? extends RuntimeException> exceptionTypeToThrow) {
        return invokeWithSpecialValue(declaringInstance, null, null, manager, creationalContext, exceptionTypeToThrow);
    }

    public T invokeWithSpecialValue(Object declaringInstance, Class<? extends Annotation> annotatedParameter, Object parameter, BeanManagerImpl manager, CreationalContext<?> ctx, Class<? extends RuntimeException> exceptionTypeToThrow) {
        CreationalContext<?> invocationContext = manager.createCreationalContext(null);
        try {
            return cast(accessibleMethod.invoke(declaringInstance, getParameterValues(annotatedParameter, parameter, manager, ctx, invocationContext)));
        } catch (IllegalArgumentException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (IllegalAccessException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (InvocationTargetException e) {
            rethrowException(e, exceptionTypeToThrow);
        } finally {
            invocationContext.release();
        }
        return null;
    }

    public T invokeOnInstance(Object declaringInstance, BeanManagerImpl manager, CreationalContext<?> creationalContext, Class<? extends RuntimeException> exceptionTypeToThrow) {
        return invokeOnInstanceWithSpecialValue(declaringInstance, null, null, manager, creationalContext, exceptionTypeToThrow);
    }

    public T invokeOnInstanceWithSpecialValue(Object declaringInstance, Class<? extends Annotation> annotatedParameter, Object parameter, BeanManagerImpl manager, CreationalContext<?> ctx, Class<? extends RuntimeException> exceptionTypeToThrow) {
        CreationalContext<?> invocationContext = manager.createCreationalContext(null);
        try {
            Method method = getMethodFromClass(declaringInstance.getClass());
            return cast(method.invoke(declaringInstance, getParameterValues(annotatedParameter, parameter, manager, ctx, invocationContext)));
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
        } finally {
            invocationContext.release();
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
    protected Object[] getParameterValues(Class<? extends Annotation> specialParam, Object specialVal, BeanManagerImpl manager, CreationalContext<?> ctx, CreationalContext<?> invocationContext) {
        Object[] parameterValues = new Object[getParameterInjectionPoints().size()];
        Iterator<ParameterInjectionPoint<?, X>> iterator = getParameterInjectionPoints().iterator();
        for (int i = 0; i < parameterValues.length; i++) {
            ParameterInjectionPoint<?, ?> param = iterator.next();
            if (specialParam != null && param.getAnnotated().isAnnotationPresent(specialParam)) {
                parameterValues[i] = specialVal;
            } else if (param.getAnnotated().isAnnotationPresent(TransientReference.class)){
                parameterValues[i] = param.getValueToInject(manager, invocationContext);
            } else {
                parameterValues[i] = param.getValueToInject(manager, ctx);
            }
        }
        return parameterValues;
    }

    @Override
    public AnnotatedMethod<X> getAnnotated() {
        return annotatedMethod;
    }

    private Method getMethodFromClass(Class<?> clazz) throws NoSuchMethodException {
        final Map<Class<?>, Method> methods = this.methods;
        Method method = this.methods.get(clazz);
        if (method == null) {
            // the same method may be written to the map twice, but that is ok
            // lookupMethod is very slow
            Method delegate = annotatedMethod.getJavaMember();
            try {
                method = AccessController.doPrivileged(new MethodLookupAction(clazz, delegate.getName(), delegate.getParameterTypes()));
                if (!Modifier.isPublic(method.getModifiers())) {
                    method = AccessController.doPrivileged(new GetAccessibleCopyOfMember<Method>(method));
                }
            } catch (PrivilegedActionException e) {
                if (e.getCause() instanceof NoSuchMethodException) {
                    throw (NoSuchMethodException) e.getCause();
                }
                throw new WeldException(e.getCause());
            }
            final Map<Class<?>, Method> newMethods = ImmutableMap.<Class<?>, Method>builder().putAll(methods).put(clazz, method).build();
            this.methods = newMethods;
        }
        return method;
    }
}