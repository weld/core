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
package org.jboss.weld.experimental;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

/**
 * This API is experimental and will change! All the methods declared by this interface are supposed to be moved to {@link InvocationContext}.
 *
 * @author Martin Kouba
 * @see CDI-468
 *
 */
public interface ExperimentalInvocationContext extends InvocationContext {

    /**
     * The returning set may be empty if only interceptors using the {@link Interceptors} annotation are associated.
     *
     * @return a set of interceptor bindings
     */
    <T extends Annotation> Set<T> getInterceptorBindingsByType(Class<T> annotationType);

    /**
     * The returning set may be empty if only interceptors using the {@link Interceptors} annotation are associated.
     *
     * @return a set of interceptor bindings
     */
    Set<Annotation> getInterceptorBindings();

}