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
package org.jboss.weld.interceptor.reader;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.interceptor.Interceptors;

import org.jboss.weld.contexts.WeldCreationalContext;
import org.jboss.weld.interceptor.spi.metadata.InterceptorFactory;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * InterceptorFactory that uses an {@link InjectionTarget} as a factory for interceptor instances.
 * <p>
 * This factory is used for interceptors that are not CDI beans - interceptors defined using the {@link Interceptors}
 * annotation.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the type of the interceptor
 */
public class PlainInterceptorFactory<T> implements InterceptorFactory<T> {

    public static <T> PlainInterceptorFactory<T> of(Class<T> javaClass, BeanManagerImpl manager) {
        AnnotatedType<T> type = manager.createAnnotatedType(javaClass);
        /*
         * For historical reasons WeldInjectionTargetFactory.createInterceptorInjectionTarget() does not add
         * resource injection support. Therefore, we intentionally use builder instead.
         */
        InjectionTarget<T> it = manager.createInjectionTargetBuilder(type)
                .setDecorationEnabled(false)
                .setInterceptionEnabled(false)
                .setResourceInjectionEnabled(true)
                .setTargetClassLifecycleCallbacksEnabled(false)
                .build();
        return new PlainInterceptorFactory<T>(it);
    }

    private final InjectionTarget<T> injectionTarget;

    public PlainInterceptorFactory(InjectionTarget<T> injectionTarget) {
        this.injectionTarget = injectionTarget;
    }

    @Override
    public T create(CreationalContext<T> ctx, BeanManagerImpl manager) {
        if (ctx instanceof WeldCreationalContext<?>) {
            WeldCreationalContext<?> weldCtx = (WeldCreationalContext<?>) ctx;
            ctx = weldCtx.getCreationalContext(null);
        }
        T instance = injectionTarget.produce(ctx);
        injectionTarget.inject(instance, ctx);
        return instance;
    }

    public InjectionTarget<T> getInjectionTarget() {
        return injectionTarget;
    }
}
