/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.interceptor.proxy;

import static org.jboss.weld.interceptor.spi.model.InterceptionType.AROUND_INVOKE;
import static org.jboss.weld.interceptor.spi.model.InterceptionType.AROUND_TIMEOUT;
import static org.jboss.weld.interceptor.spi.model.InterceptionType.POST_ACTIVATE;
import static org.jboss.weld.interceptor.spi.model.InterceptionType.POST_CONSTRUCT;
import static org.jboss.weld.interceptor.spi.model.InterceptionType.PRE_DESTROY;
import static org.jboss.weld.interceptor.spi.model.InterceptionType.PRE_PASSIVATE;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.interceptor.reader.TargetClassInterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorClassMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.collections.WeldCollections;

/**
 * Holds interceptor metadata and interceptor instances throughout the lifecycle of the intercepted instance.
 *
 * @author Jozef Hartinger
 * @author Martin Kouba
 *
 */
public class InterceptionContext implements Serializable {

    private static final Set<InterceptionType> METHOD_INTERCEPTION_TYPES = ImmutableSet.of(AROUND_INVOKE, AROUND_TIMEOUT,
            POST_CONSTRUCT, PRE_DESTROY, POST_ACTIVATE, PRE_PASSIVATE);

    /**
     * The context returned by this method may be later reused for other interception types.
     *
     * @param interceptionModel
     * @param ctx
     * @param manager
     * @param type
     * @return the interception context to be used for the AroundConstruct chain
     */
    public static InterceptionContext forConstructorInterception(InterceptionModel interceptionModel, CreationalContext<?> ctx,
            BeanManagerImpl manager, SlimAnnotatedType<?> type) {
        return of(interceptionModel, ctx, manager, null, type);
    }

    public static InterceptionContext forNonConstructorInterception(InterceptionModel interceptionModel,
            CreationalContext<?> ctx, BeanManagerImpl manager, SlimAnnotatedType<?> type) {
        return of(interceptionModel, ctx, manager, METHOD_INTERCEPTION_TYPES, type);
    }

    private static InterceptionContext of(InterceptionModel interceptionModel, CreationalContext<?> ctx,
            BeanManagerImpl manager, Set<InterceptionType> interceptionTypes, SlimAnnotatedType<?> type) {
        return new InterceptionContext(initInterceptorInstanceMap(interceptionModel, ctx, manager, interceptionTypes), manager,
                interceptionModel, type);
    }

    private static final long serialVersionUID = 7500722360133273633L;

    private final transient InterceptionModel interceptionModel;

    private final Map<Serializable, Object> interceptorInstances;
    private final BeanManagerImpl manager;
    private final SlimAnnotatedType<?> annotatedType;

    private InterceptionContext(Map<Serializable, Object> interceptorInstances, BeanManagerImpl manager,
            InterceptionModel interceptionModel, SlimAnnotatedType<?> type) {
        this.interceptorInstances = interceptorInstances;
        this.manager = manager;
        this.interceptionModel = interceptionModel;
        this.annotatedType = type;
    }

    private static Map<Serializable, Object> initInterceptorInstanceMap(InterceptionModel model, CreationalContext ctx,
            BeanManagerImpl manager,
            Set<InterceptionType> interceptionTypes) {
        Map<Serializable, Object> interceptorInstances = new HashMap<>();
        for (InterceptorClassMetadata<?> interceptor : model.getAllInterceptors()) {
            if (interceptionTypes != null) {
                for (InterceptionType interceptionType : interceptionTypes) {
                    if (interceptor.isEligible(interceptionType) && !interceptorInstances.containsKey(interceptor.getKey())) {
                        interceptorInstances.put(interceptor.getKey(),
                                interceptor.getInterceptorFactory().create(ctx, manager));
                    }
                }
            } else {
                interceptorInstances.put(interceptor.getKey(), interceptor.getInterceptorFactory().create(ctx, manager));
            }
        }
        return WeldCollections.immutableMapView(interceptorInstances);
    }

    public InterceptionModel getInterceptionModel() {
        return interceptionModel;
    }

    public <T> T getInterceptorInstance(InterceptorClassMetadata<T> interceptorMetadata) {
        return cast(interceptorInstances.get(interceptorMetadata.getKey()));
    }

    private Object readResolve() throws ObjectStreamException {
        InterceptionModel interceptionModel = manager.getInterceptorModelRegistry().get(annotatedType);
        return new InterceptionContext(interceptorInstances, manager, interceptionModel, annotatedType);
    }

    public List<InterceptorMethodInvocation> buildInterceptorMethodInvocations(Object instance, Method method,
            InterceptionType interceptionType) {
        List<? extends InterceptorClassMetadata<?>> interceptorList = interceptionModel.getInterceptors(interceptionType,
                method);
        List<InterceptorMethodInvocation> interceptorInvocations = new ArrayList<InterceptorMethodInvocation>(
                interceptorList.size());
        for (InterceptorClassMetadata<?> interceptorMetadata : interceptorList) {
            interceptorInvocations.addAll(
                    interceptorMetadata.getInterceptorInvocation(getInterceptorInstance(interceptorMetadata), interceptionType)
                            .getInterceptorMethodInvocations());
        }
        TargetClassInterceptorMetadata targetClassInterceptorMetadata = getInterceptionModel()
                .getTargetClassInterceptorMetadata();
        if (targetClassInterceptorMetadata != null && targetClassInterceptorMetadata.isEligible(interceptionType)) {
            interceptorInvocations
                    .addAll(targetClassInterceptorMetadata.getInterceptorInvocation(instance, interceptionType)
                            .getInterceptorMethodInvocations());
        }
        return ImmutableList.copyOf(interceptorInvocations);
    }

    public List<InterceptorMethodInvocation> buildInterceptorMethodInvocationsForConstructorInterception() {
        List<? extends InterceptorClassMetadata<?>> interceptorList = interceptionModel.getConstructorInvocationInterceptors();
        List<InterceptorMethodInvocation> interceptorInvocations = new ArrayList<InterceptorMethodInvocation>(
                interceptorList.size());
        for (InterceptorClassMetadata<?> metadata : interceptorList) {
            Object interceptorInstance = getInterceptorInstance(metadata);
            InterceptorInvocation invocation = metadata.getInterceptorInvocation(interceptorInstance,
                    InterceptionType.AROUND_CONSTRUCT);
            interceptorInvocations.addAll(invocation.getInterceptorMethodInvocations());
        }
        return ImmutableList.copyOf(interceptorInvocations);
    }
}
