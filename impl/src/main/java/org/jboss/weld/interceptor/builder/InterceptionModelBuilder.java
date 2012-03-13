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

import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.interceptor.spi.model.InterceptionType;


/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class InterceptionModelBuilder<T, I> {

    private final BuildableInterceptionModel<T, I> interceptionModel;

    private final T interceptedEntity;

    private InterceptionModelBuilder(T interceptedEntity) {
        this.interceptedEntity = interceptedEntity;
        this.interceptionModel = new InterceptionModelImpl<T, I>(interceptedEntity);
    }

    public static <T> InterceptionModelBuilder<T, ?> newBuilderFor(T entity) {
        return new InterceptionModelBuilder<T, Object>(entity);
    }

    public T getInterceptedEntity() {
        return interceptedEntity;
    }

    public InterceptionModel<T, I> build() {
        return interceptionModel;
    }

    public MethodInterceptorDescriptor interceptAll() {
        return new MethodInterceptorDescriptor(null, InterceptionType.values());
    }

    public MethodInterceptorDescriptor interceptAroundInvoke(Method m) {
        return intercept(javax.enterprise.inject.spi.InterceptionType.AROUND_INVOKE, m);
    }

    public MethodInterceptorDescriptor interceptAroundTimeout(Method m) {
        return intercept(javax.enterprise.inject.spi.InterceptionType.AROUND_TIMEOUT, m);
    }

    public MethodInterceptorDescriptor intercept(javax.enterprise.inject.spi.InterceptionType interceptionType, Method m) {
        InterceptionType weldInterceptionType = InterceptionType.valueOf(interceptionType);
        if (weldInterceptionType.isLifecycleCallback()) {
            throw new IllegalArgumentException("Illegal interception type: " + interceptionType);
        }
        return new MethodInterceptorDescriptor(m, weldInterceptionType);
    }

    public MethodInterceptorDescriptor interceptPostConstruct() {
        return intercept(javax.enterprise.inject.spi.InterceptionType.POST_CONSTRUCT);
    }

    public MethodInterceptorDescriptor interceptPreDestroy() {
        return intercept(javax.enterprise.inject.spi.InterceptionType.PRE_DESTROY);
    }

    public MethodInterceptorDescriptor interceptPrePassivate() {
        return intercept(javax.enterprise.inject.spi.InterceptionType.PRE_PASSIVATE);
    }

    public MethodInterceptorDescriptor interceptPostActivate() {
        return intercept(javax.enterprise.inject.spi.InterceptionType.POST_ACTIVATE);
    }

    public MethodInterceptorDescriptor intercept(javax.enterprise.inject.spi.InterceptionType interceptionType) {
        InterceptionType weldInterceptionType = InterceptionType.valueOf(interceptionType);
        if (!weldInterceptionType.isLifecycleCallback()) {
            throw new IllegalArgumentException("Illegal interception type: " + interceptionType);
        }
        return new MethodInterceptorDescriptor(null, weldInterceptionType);
    }

    public void ignoreGlobalInterceptors(Method m) {
        this.interceptionModel.setIgnoresGlobals(m, true);
    }

    public final class MethodInterceptorDescriptor {
        private final Method method;

        private final InterceptionType[] interceptionTypes;

        public MethodInterceptorDescriptor(Method m, InterceptionType... interceptionType) {
            this.method = m;
            this.interceptionTypes = interceptionType;
        }

        public void with(InterceptorMetadata... interceptors) {
            for (InterceptionType interceptionType : interceptionTypes) {
                InterceptionModelBuilder.this.interceptionModel.appendInterceptors(interceptionType, method, interceptors);
            }
        }
    }

}
