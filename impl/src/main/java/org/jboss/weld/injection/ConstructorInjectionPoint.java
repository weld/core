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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.ConstructorSignature;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.runtime.RuntimeAnnotatedMembers;
import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * High-level representation of an injected constructor. This class does not need to be serializable because it is never injected.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class ConstructorInjectionPoint<T> extends AbstractCallableInjectionPoint<T, T, Constructor<T>> {

    private final AnnotatedConstructor<T> constructor;
    private final ConstructorSignature signature;

    protected ConstructorInjectionPoint(EnhancedAnnotatedConstructor<T> constructor, Bean<T> declaringBean, Class<?> declaringComponentClass, InjectionPointFactory factory, BeanManagerImpl manager) {
        super(constructor, declaringBean, declaringComponentClass, false, factory, manager);
        this.constructor = constructor.slim();
        this.signature = constructor.getSignature();
    }

    public T newInstance(BeanManagerImpl manager, CreationalContext<?> creationalContext) {
        return newInstance(manager, creationalContext, getParameterValues(getParameterInjectionPoints(), null, null, manager, creationalContext));
    }

    public T newInstance(BeanManagerImpl manager, CreationalContext<?> creationalContext, Object[] parameterValues) {
        try {
            return RuntimeAnnotatedMembers.newInstance(getAnnotated(), parameterValues);
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

    public void inject(Object declaringInstance, Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Helper method for getting the current parameter values from a list of annotated parameters.
     *
     * @param parameters The list of annotated parameter to look up
     * @param manager The Bean manager
     * @return The object array of looked up values
     */
    protected Object[] getParameterValues(List<ParameterInjectionPoint<?, T>> parameters, Object specialVal, Class<? extends Annotation> specialParam, BeanManagerImpl manager,
            CreationalContext<?> creationalContext) {
        Object[] parameterValues = new Object[parameters.size()];
        Iterator<ParameterInjectionPoint<?, T>> iterator = parameters.iterator();
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

    public AnnotatedConstructor<T> getAnnotated() {
        return constructor;
    }

    public ConstructorSignature getSignature() {
        return signature;
    }
}
