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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.TransientReference;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.ConstructorSignature;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.construction.api.AroundConstructCallback;
import org.jboss.weld.construction.api.ConstructionHandle;
import org.jboss.weld.contexts.CreationalContextImpl;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.security.GetAccessibleCopyOfMember;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * High-level representation of an injected constructor. This class does not need to be serializable because it is never
 * injected.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 *
 * @param <T>
 */
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public class ConstructorInjectionPoint<T> extends AbstractCallableInjectionPoint<T, T, Constructor<T>> {

    private final AnnotatedConstructor<T> constructor;
    private final ConstructorSignature signature;
    private final Constructor<T> accessibleConstructor;

    protected ConstructorInjectionPoint(EnhancedAnnotatedConstructor<T> constructor, Bean<T> declaringBean,
            Class<?> declaringComponentClass, InjectionPointFactory factory, BeanManagerImpl manager) {
        super(constructor, declaringBean, declaringComponentClass, false, factory, manager);
        this.constructor = constructor.slim();
        this.signature = constructor.getSignature();
        this.accessibleConstructor = AccessController
                .doPrivileged(new GetAccessibleCopyOfMember<Constructor<T>>(constructor.getJavaMember()));
    }

    public T newInstance(BeanManagerImpl manager, CreationalContext<?> ctx) {
        CreationalContext<?> transientReferenceContext = null;
        if (hasTransientReferenceParameter) {
            transientReferenceContext = manager.createCreationalContext(null);
        }
        try {
            Object[] parameterValues = getParameterValues(manager, ctx, transientReferenceContext);
            if (ctx instanceof CreationalContextImpl<?>) {
                CreationalContextImpl<T> weldCtx = Reflections.cast(ctx);
                return invokeAroundConstructCallbacks(parameterValues, weldCtx);
            } else {
                return newInstance(parameterValues);
            }
        } finally {
            if (hasTransientReferenceParameter) {
                transientReferenceContext.release();
            }
        }
    }

    private T invokeAroundConstructCallbacks(Object[] parameters, CreationalContextImpl<T> ctx) {
        final List<AroundConstructCallback<T>> callbacks = ctx.getAroundConstructCallbacks();
        if (callbacks.isEmpty()) {
            return newInstance(parameters);
        }
        final Iterator<AroundConstructCallback<T>> iterator = callbacks.iterator();
        return invokeAroundConstructCallback(iterator.next(), new ConstructionHandle<T>() {
            @Override
            public T proceed(Object[] parameters, Map<String, Object> data) {
                if (iterator.hasNext()) {
                    return invokeAroundConstructCallback(iterator.next(), this, getComponentConstructor(), parameters, data);
                } else {
                    return newInstance(parameters);
                }
            }
        }, getComponentConstructor(), parameters, new HashMap<String, Object>());
    }

    private T invokeAroundConstructCallback(AroundConstructCallback<T> callback, ConstructionHandle<T> ctx,
            AnnotatedConstructor<T> constructor, Object[] parameters, Map<String, Object> data) {
        try {
            return callback.aroundConstruct(ctx, constructor, parameters, data);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    protected T newInstance(Object[] parameterValues) {
        try {
            return accessibleConstructor.newInstance(parameterValues);
        } catch (IllegalArgumentException e) {
            rethrowException(e);
        } catch (InstantiationException e) {
            rethrowException(e);
        } catch (IllegalAccessException e) {
            rethrowException(e);
        } catch (InvocationTargetException e) {
            rethrowException(e);
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
    public Object[] getParameterValues(BeanManagerImpl manager, CreationalContext<?> ctx,
            CreationalContext<?> transientReference) {
        if (getInjectionPoints().isEmpty()) {
            return Arrays2.EMPTY_ARRAY;
        }
        Object[] parameterValues = new Object[getParameterInjectionPoints().size()];
        List<ParameterInjectionPoint<?, T>> parameters = getParameterInjectionPoints();
        for (int i = 0; i < parameterValues.length; i++) {
            ParameterInjectionPoint<?, ?> param = parameters.get(i);
            if (hasTransientReferenceParameter && param.getAnnotated().isAnnotationPresent(TransientReference.class)) {
                parameterValues[i] = param.getValueToInject(manager, transientReference);
            } else {
                parameterValues[i] = param.getValueToInject(manager, ctx);
            }
        }
        return parameterValues;
    }

    public AnnotatedConstructor<T> getAnnotated() {
        return constructor;
    }

    public ConstructorSignature getSignature() {
        return signature;
    }

    public AnnotatedConstructor<T> getComponentConstructor() {
        return constructor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((constructor == null) ? 0 : constructor.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConstructorInjectionPoint<?> other = (ConstructorInjectionPoint<?>) obj;
        if (constructor == null) {
            if (other.constructor != null) {
                return false;
            }
        } else if (!constructor.equals(other.constructor)) {
            return false;
        }
        return true;
    }
}
