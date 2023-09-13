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
package org.jboss.weld.bean.interceptor;

import java.io.Serializable;

import jakarta.enterprise.inject.spi.Interceptor;

import org.jboss.weld.interceptor.proxy.CustomInterceptorInvocation;
import org.jboss.weld.interceptor.proxy.InterceptorInvocation;
import org.jboss.weld.interceptor.spi.metadata.InterceptorClassMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionType;

/**
 * @author Marius Bogoevici
 */
public class CustomInterceptorMetadata<T> implements InterceptorClassMetadata<T> {

    @SuppressWarnings("unchecked")
    public static <T> CustomInterceptorMetadata<T> of(Interceptor<T> interceptor) {
        return new CustomInterceptorMetadata<T>(new CdiInterceptorFactory<T>(interceptor),
                (Class<T>) interceptor.getBeanClass(),
                interceptor.getName() != null && !interceptor.getName().isEmpty() ? interceptor.getName() : null);
    }

    private final CdiInterceptorFactory<T> factory;
    private final Class<T> javaClass;
    private final String key;

    private CustomInterceptorMetadata(CdiInterceptorFactory<T> factory, Class<T> javaClass, String key) {
        this.factory = factory;
        this.javaClass = javaClass;
        this.key = key;
    }

    @Override
    public CdiInterceptorFactory<T> getInterceptorFactory() {
        return factory;
    }

    @Override
    public boolean isEligible(InterceptionType interceptionType) {
        return factory.getInterceptor()
                .intercepts(jakarta.enterprise.inject.spi.InterceptionType.valueOf(interceptionType.name()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public InterceptorInvocation getInterceptorInvocation(Object interceptorInstance, InterceptionType interceptionType) {
        return new CustomInterceptorInvocation<T>(factory.getInterceptor(), (T) interceptorInstance,
                jakarta.enterprise.inject.spi.InterceptionType.valueOf(interceptionType.name()));
    }

    @Override
    public String toString() {
        return "CustomInterceptorMetadata [" + getJavaClass().getName() + "]";
    }

    @Override
    public Class<T> getJavaClass() {
        return javaClass;
    }

    @Override
    public Serializable getKey() {
        return key != null ? key : InterceptorClassMetadata.super.getKey();
    }

}
