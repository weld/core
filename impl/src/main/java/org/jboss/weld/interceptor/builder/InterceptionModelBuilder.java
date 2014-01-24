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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.interceptor.reader.TargetClassInterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorClassMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.interceptor.spi.model.InterceptionType;


/**
 * This builder shouldn't be reused.
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author Martin Kouba
 *
 */
public class InterceptionModelBuilder {

    private boolean isModelBuilt = false;

    private boolean hasExternalNonConstructorInterceptors;

    private final Set<Method> methodsIgnoringGlobalInterceptors = new HashSet<Method>();

    private final Set<InterceptorClassMetadata<?>> allInterceptors = new LinkedHashSet<InterceptorClassMetadata<?>>();

    private final Map<InterceptionType, List<InterceptorClassMetadata<?>>> globalInterceptors = new HashMap<InterceptionType, List<InterceptorClassMetadata<?>>>();

    private final Map<InterceptionType, Map<Method, List<InterceptorClassMetadata<?>>>> methodBoundInterceptors = new HashMap<InterceptionType, Map<Method, List<InterceptorClassMetadata<?>>>>();

    private TargetClassInterceptorMetadata targetClassInterceptorMetadata;

    /**
     * @return an immutable {@link InterceptionModel} instance
     */
    public InterceptionModel build() {
        checkModelNotBuilt();
        isModelBuilt = true;
        return new InterceptionModelImpl(this);
    }

    public void intercept(javax.enterprise.inject.spi.InterceptionType interceptionType, Method method, Collection<InterceptorClassMetadata<?>> interceptors) {
        checkModelNotBuilt();
        InterceptionType weldInterceptionType = InterceptionType.valueOf(interceptionType);
        if (weldInterceptionType.isLifecycleCallback()) {
            throw new IllegalArgumentException("Illegal interception type: " + interceptionType);
        }

        if (null == methodBoundInterceptors.get(weldInterceptionType)) {
            methodBoundInterceptors.put(weldInterceptionType, new HashMap<Method, List<InterceptorClassMetadata<?>>>());
        }
        List<InterceptorClassMetadata<?>> interceptorsList = methodBoundInterceptors.get(weldInterceptionType).get(method);
        if (interceptorsList == null) {
            interceptorsList = new ArrayList<InterceptorClassMetadata<?>>();
            methodBoundInterceptors.get(weldInterceptionType).put(method, interceptorsList);
        }
        interceptorsList.addAll(interceptors);
        intercept(weldInterceptionType, interceptorsList);
    }

    public void intercept(javax.enterprise.inject.spi.InterceptionType interceptionType, Collection<InterceptorClassMetadata<?>> interceptors) {
        checkModelNotBuilt();
        InterceptionType weldInterceptionType = InterceptionType.valueOf(interceptionType);

        List<InterceptorClassMetadata<?>> interceptorsList = globalInterceptors.get(weldInterceptionType);
        if (interceptorsList == null) {
            interceptorsList = new ArrayList<InterceptorClassMetadata<?>>();
            globalInterceptors.put(weldInterceptionType, interceptorsList);
        }
        interceptorsList.addAll(interceptors);
        intercept(weldInterceptionType, interceptorsList);
    }

    private void intercept(InterceptionType interceptionType, Collection<InterceptorClassMetadata<?>> interceptors) {
        if (interceptionType != InterceptionType.AROUND_CONSTRUCT) {
            hasExternalNonConstructorInterceptors = true;
        }
        allInterceptors.addAll(interceptors);
    }

    public void addMethodIgnoringGlobalInterceptors(Method method) {
        checkModelNotBuilt();
        this.methodsIgnoringGlobalInterceptors.add(method);
    }

    boolean isHasExternalNonConstructorInterceptors() {
        return hasExternalNonConstructorInterceptors;
    }

    Set<Method> getMethodsIgnoringGlobalInterceptors() {
        return methodsIgnoringGlobalInterceptors;
    }

    Set<InterceptorClassMetadata<?>> getAllInterceptors() {
        return allInterceptors;
    }

    Map<InterceptionType, List<InterceptorClassMetadata<?>>> getGlobalInterceptors() {
        return globalInterceptors;
    }

    Map<InterceptionType, Map<Method, List<InterceptorClassMetadata<?>>>> getMethodBoundInterceptors() {
        return methodBoundInterceptors;
    }

    private void checkModelNotBuilt() {
        if(isModelBuilt) {
            throw new IllegalStateException("InterceptionModelBuilder cannot be reused");
        }
    }

    TargetClassInterceptorMetadata getTargetClassInterceptorMetadata() {
        return targetClassInterceptorMetadata;
    }

    public void setTargetClassInterceptorMetadata(TargetClassInterceptorMetadata targetClassInterceptorMetadata) {
        this.targetClassInterceptorMetadata = targetClassInterceptorMetadata;
    }

}
