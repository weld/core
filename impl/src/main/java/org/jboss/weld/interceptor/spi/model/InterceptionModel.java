/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.interceptor.spi.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import jakarta.interceptor.AroundConstruct;

import org.jboss.weld.interceptor.reader.TargetClassInterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorClassMetadata;

/**
 * Describes the {@link org.jboss.weld.interceptor.spi.metadata.InterceptorClassMetadata}s that apply to a particular entity.
 * <p/>
 * Implementors must implement equals() and hashcode() consistently
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public interface InterceptionModel {

    /**
     * Returns the interceptors applicable for the given interception type and method. For resolving {@link AroundConstruct}
     * interceptors use {@link #getConstructorInvocationInterceptors(java.lang.reflect.Constructor)}.
     *
     * @param interceptionType
     * @param method - null if the interception type is lifecycle
     * @return list of interceptors
     * @throws IllegalArgumentException if interceptionType is business method or around timeout
     *         but method is null, as well as if interceptionType is callback and method is not null
     */
    List<InterceptorClassMetadata<?>> getInterceptors(InterceptionType interceptionType, Method method);

    /**
     * Returns {@link AroundConstruct} interceptors applicable for the given constructor.
     */
    List<InterceptorClassMetadata<?>> getConstructorInvocationInterceptors();

    /**
     * Returns all interceptor classes that are applicable to the given intercepted entity
     *
     * @return all interceptors
     */
    Set<InterceptorClassMetadata<?>> getAllInterceptors();

    /**
     * Indicates whether the given entity has associated {@link AroundConstruct} interceptors.
     */
    boolean hasExternalConstructorInterceptors();

    /**
     * Indicates whether the given entity has an associated interceptor of a kind other than {@link AroundConstruct}
     */
    boolean hasExternalNonConstructorInterceptors();

    /**
     * Indicates whether the given entity has target class interceptor methods.
     */
    boolean hasTargetClassInterceptors();

    /**
     * Returns the interceptor metadata for the component class of this component.
     *
     * @return interceptor metadata for the component class
     */
    TargetClassInterceptorMetadata getTargetClassInterceptorMetadata();

    /**
     *
     * @return class-level interceptor bindings
     */
    Set<Annotation> getClassInterceptorBindings();

    /**
     * @param method
     * @return method/constructor interceptor bindings
     */
    Set<Annotation> getMemberInterceptorBindings(Member member);

}
