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

import java.lang.reflect.Method;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedCallable;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Wraps a method whose parameters may be injected.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the return type of the method
 * @param <X> the type of the class that declared the method
 */
public abstract class MethodInjectionPoint<T, X> extends AbstractCallableInjectionPoint<T, X, Method> {

    protected MethodInjectionPointType type;

    protected MethodInjectionPoint(MethodInjectionPointType methodInjectionPointType,
            EnhancedAnnotatedCallable<T, X, Method> callable, Bean<?> declaringBean, Class<?> declaringComponentClass,
            InjectionPointFactory factory, BeanManagerImpl manager) {
        super(callable, declaringBean, declaringComponentClass,
                MethodInjectionPointType.OBSERVER.equals(methodInjectionPointType)
                        || MethodInjectionPointType.DISPOSER.equals(methodInjectionPointType),
                factory, manager);
        this.type = methodInjectionPointType;
    }

    /**
     * Invokes the method.
     *
     * @param receiver the instance to receive the method invocation or null if this is a static method
     * @param specialValue value to be passed to the special parameter (observer or disposer parameter) or null if the method is
     *        not an observer or disposer
     * @param manager the bean manager
     * @param ctx the creational context
     * @param exceptionTypeToThrow exception type to be used to wrap potential exceptions within
     * @return the value returned from the method
     */
    public abstract T invoke(Object receiver, Object specialValue, BeanManagerImpl manager, CreationalContext<?> ctx,
            Class<? extends RuntimeException> exceptionTypeToThrow);

    abstract T invoke(Object receiver, Object[] parameters, Class<? extends RuntimeException> exceptionTypeToThrow);

    @Override
    public abstract AnnotatedMethod<X> getAnnotated();

    public static enum MethodInjectionPointType {

        INITIALIZER,
        PRODUCER,
        DISPOSER,
        OBSERVER;

    }

}
