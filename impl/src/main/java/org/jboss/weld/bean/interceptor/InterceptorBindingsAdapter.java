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

package org.jboss.weld.bean.interceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;

import org.jboss.weld.ejb.spi.InterceptorBindings;
import org.jboss.weld.interceptor.spi.metadata.InterceptorClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorFactory;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.logging.BeanLogger;

/**
 * @author Marius Bogoevici
 */
public class InterceptorBindingsAdapter implements InterceptorBindings {

    private InterceptionModel interceptionModel;

    public InterceptorBindingsAdapter(InterceptionModel interceptionModel) {
        if (interceptionModel == null) {
            throw BeanLogger.LOG.interceptionModelNull();
        }
        this.interceptionModel = interceptionModel;
    }

    @Override
    public Collection<Interceptor<?>> getAllInterceptors() {
        Set<? extends InterceptorClassMetadata<?>> interceptorMetadataSet = interceptionModel.getAllInterceptors();
        return extractCdiInterceptors(interceptorMetadataSet);
    }

    @Override
    public List<Interceptor<?>> getMethodInterceptors(InterceptionType interceptionType, Method method) {
        if (interceptionType == null) {
            throw BeanLogger.LOG.interceptionTypeNull();
        }

        if (method == null) {
            throw BeanLogger.LOG.methodNull();
        }

        org.jboss.weld.interceptor.spi.model.InterceptionType internalInterceptionType = org.jboss.weld.interceptor.spi.model.InterceptionType
                .valueOf(interceptionType.name());

        if (internalInterceptionType.isLifecycleCallback()) {
            throw BeanLogger.LOG.interceptionTypeLifecycle(interceptionType.name());
        }

        return extractCdiInterceptors(interceptionModel.getInterceptors(internalInterceptionType, method));

    }

    @Override
    public List<Interceptor<?>> getLifecycleInterceptors(InterceptionType interceptionType) {
        if (interceptionType == null) {
            throw BeanLogger.LOG.interceptionTypeNull();
        }

        org.jboss.weld.interceptor.spi.model.InterceptionType internalInterceptionType = org.jboss.weld.interceptor.spi.model.InterceptionType
                .valueOf(interceptionType.name());

        if (!internalInterceptionType.isLifecycleCallback()) {
            throw BeanLogger.LOG.interceptionTypeNotLifecycle(interceptionType.name());
        }
        if (internalInterceptionType.equals(org.jboss.weld.interceptor.spi.model.InterceptionType.AROUND_CONSTRUCT)) {
            return extractCdiInterceptors(interceptionModel.getConstructorInvocationInterceptors());
        }

        return extractCdiInterceptors(interceptionModel.getInterceptors(internalInterceptionType, null));
    }

    private List<Interceptor<?>> extractCdiInterceptors(
            Collection<? extends InterceptorClassMetadata<?>> interceptorMetadatas) {
        // ignore interceptors which are not CDI interceptors
        ArrayList<Interceptor<?>> interceptors = new ArrayList<Interceptor<?>>();
        for (InterceptorClassMetadata<?> interceptorMetadata : interceptorMetadatas) {
            InterceptorFactory<?> interceptorFactory = interceptorMetadata.getInterceptorFactory();
            if (interceptorFactory instanceof CdiInterceptorFactory<?>) {
                CdiInterceptorFactory<?> cdiInterceptorFactory = (CdiInterceptorFactory<?>) interceptorFactory;
                interceptors.add(cdiInterceptorFactory.getInterceptor());
            }
        }
        return interceptors;
    }

}
