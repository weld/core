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

package org.jboss.weld.interceptor.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.interceptor.reader.TargetClassInterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorClassMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * This impl is immutable provided the type of the intercepted entity is immutable as well.
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author Martin Kouba
 *
 * @param <T> the type of the intercepted entity
 */
class InterceptionModelImpl implements InterceptionModel {

    private final Map<InterceptionType, List<InterceptorClassMetadata<?>>> globalInterceptors;

    private final Map<InterceptionType, Map<Method, List<InterceptorClassMetadata<?>>>> methodBoundInterceptors;

    private final Set<Method> methodsIgnoringGlobalInterceptors;

    private final Set<InterceptorClassMetadata<?>> allInterceptors;

    private final boolean hasExternalNonConstructorInterceptors;

    private final TargetClassInterceptorMetadata targetClassInterceptorMetadata;

    private final Map<Member, Set<Annotation>> memberInterceptorBindings;

    private final Set<Annotation> classInterceptorBindings;

    InterceptionModelImpl(InterceptionModelBuilder builder) {
        this.hasExternalNonConstructorInterceptors = builder.isHasExternalNonConstructorInterceptors();
        this.globalInterceptors = builder.getGlobalInterceptors();
        this.methodBoundInterceptors = builder.getMethodBoundInterceptors();
        this.methodsIgnoringGlobalInterceptors = builder.getMethodsIgnoringGlobalInterceptors();
        this.allInterceptors = builder.getAllInterceptors();
        this.targetClassInterceptorMetadata = builder.getTargetClassInterceptorMetadata();
        this.memberInterceptorBindings = builder.getMemberInterceptorBindings();
        this.classInterceptorBindings = ImmutableSet.copyOf(builder.getClassInterceptorBindings());
    }

    @Override
    public List<InterceptorClassMetadata<?>> getInterceptors(InterceptionType interceptionType, Method method) {
        if (InterceptionType.AROUND_CONSTRUCT.equals(interceptionType)) {
            throw new IllegalStateException(
                    "Cannot use getInterceptors() for @AroundConstruct interceptor lookup. Use getConstructorInvocationInterceptors() instead.");
        }
        if (interceptionType.isLifecycleCallback() && method != null) {
            throw new IllegalArgumentException("On a lifecycle callback, the associated method must be null");
        }

        if (!interceptionType.isLifecycleCallback() && method == null) {
            throw new IllegalArgumentException("Around-invoke and around-timeout interceptors are defined for a given method");
        }

        if (interceptionType.isLifecycleCallback()) {
            if (globalInterceptors.containsKey(interceptionType)) {
                return globalInterceptors.get(interceptionType);
            }
        } else {
            ArrayList<InterceptorClassMetadata<?>> returnedInterceptors = new ArrayList<InterceptorClassMetadata<?>>();
            if (!methodsIgnoringGlobalInterceptors.contains(method) && globalInterceptors.containsKey(interceptionType)) {
                returnedInterceptors.addAll(globalInterceptors.get(interceptionType));
            }
            Map<Method, List<InterceptorClassMetadata<?>>> map = methodBoundInterceptors.get(interceptionType);
            if (map != null) {
                List<InterceptorClassMetadata<?>> list = map.get(method);
                if (list != null) {
                    returnedInterceptors.addAll(list);
                }
            }
            return returnedInterceptors;
        }
        return Collections.emptyList();
    }

    @Override
    public Set<InterceptorClassMetadata<?>> getAllInterceptors() {
        return allInterceptors;
    }

    @Override
    public List<InterceptorClassMetadata<?>> getConstructorInvocationInterceptors() {
        if (globalInterceptors.containsKey(InterceptionType.AROUND_CONSTRUCT)) {
            return globalInterceptors.get(InterceptionType.AROUND_CONSTRUCT);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasExternalConstructorInterceptors() {
        return !getConstructorInvocationInterceptors().isEmpty();
    }

    @Override
    public boolean hasExternalNonConstructorInterceptors() {
        return hasExternalNonConstructorInterceptors;
    }

    @Override
    public boolean hasTargetClassInterceptors() {
        return targetClassInterceptorMetadata != null
                && targetClassInterceptorMetadata != TargetClassInterceptorMetadata.EMPTY_INSTANCE;
    }

    @Override
    public TargetClassInterceptorMetadata getTargetClassInterceptorMetadata() {
        return targetClassInterceptorMetadata;
    }

    @Override
    public Set<Annotation> getClassInterceptorBindings() {
        return classInterceptorBindings;
    }

    @Override
    public Set<Annotation> getMemberInterceptorBindings(Member member) {
        return memberInterceptorBindings.get(member);
    }

}
