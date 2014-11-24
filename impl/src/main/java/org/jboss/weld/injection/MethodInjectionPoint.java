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

import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Wraps a method whose parameters may be injected.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the return type of the method
 * @param <X> the type of the class that declared the method
 */
public interface MethodInjectionPoint<T, X> {

    /**
     * Invokes the method.
     *
     * @param receiver the instance to receive the method invocation or null if this is a static method
     * @param specialValue value to be passed to the special parameter (observer or disposer parameter) or null if the method is not an observer or disposer
     * @param manager the bean manager
     * @param ctx the creational context
     * @param exceptionTypeToThrow exception type to be used to wrap potential exceptions within
     * @return the value returned from the method
     */
    T invoke(Object receiver, Object specialValue, BeanManagerImpl manager, CreationalContext<?> ctx, Class<? extends RuntimeException> exceptionTypeToThrow);

    AnnotatedMethod<X> getAnnotated();

    List<ParameterInjectionPoint<?, X>> getParameterInjectionPoints();

    Set<InjectionPoint> getInjectionPoints();
}
