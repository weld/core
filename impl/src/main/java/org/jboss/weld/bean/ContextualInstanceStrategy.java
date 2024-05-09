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
package org.jboss.weld.bean;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.inject.Singleton;

import org.jboss.weld.contexts.cache.RequestScopedCache;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

/**
 * This component allows optimized strategies for obtaining contextual instances of a given bean to be plugged in.
 *
 * By default a contextual instance of a bean is obtained by first obtaining the context for bean's scope and then by
 * calling {@link Context#get(jakarta.enterprise.context.spi.Contextual)} or
 * {@link Context#get(jakarta.enterprise.context.spi.Contextual, CreationalContext)}
 * on the given context. This algorithm matches the {@link #defaultStrategy()} implementation.
 *
 * In addition, specialized implementations are provided.
 *
 * For {@link ApplicationScoped} beans a special strategy is used which caches application-scoped bean instances in a volatile
 * field. This implementation respects
 * the possibility of an instance being destroyed via {@link AlterableContext} and the cached instance is flushed in such case.
 *
 * For {@link SessionScoped}, {@link ConversationScoped} and {@link RequestScoped} beans a special strategy is used which caches
 * contextual bean instances in
 * a {@link ThreadLocal}. This implementation respects the possibility of an instance being destroyed via
 * {@link AlterableContext} and the cached instance is
 * flushed in such case. This is done indirectly by {@link RequestScopedCache}.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public abstract class ContextualInstanceStrategy<T> {

    @SuppressWarnings("unchecked")
    public static <T> ContextualInstanceStrategy<T> defaultStrategy() {
        return (ContextualInstanceStrategy<T>) DefaultContextualInstanceStrategy.INSTANCE;
    }

    public static <T> ContextualInstanceStrategy<T> create(BeanAttributes<T> bean, BeanManagerImpl manager) {
        if (ApplicationScoped.class == bean.getScope() || Singleton.class == bean.getScope()) {
            return new ApplicationScopedContextualInstanceStrategy<T>();
        } else if (CachingContextualInstanceStrategy.CACHEABLE_SCOPES.contains(bean.getScope())) {
            return new CachingContextualInstanceStrategy<T>();
        }
        return defaultStrategy();
    }

    ContextualInstanceStrategy() {
    }

    abstract T get(Bean<T> bean, BeanManagerImpl manager, CreationalContext<?> ctx);

    abstract T getIfExists(Bean<T> bean, BeanManagerImpl manager);

    abstract void destroy(Bean<T> bean);

    private static class DefaultContextualInstanceStrategy<T> extends ContextualInstanceStrategy<T> {

        static final ContextualInstanceStrategy<Object> INSTANCE = new DefaultContextualInstanceStrategy<Object>();

        @Override
        T getIfExists(Bean<T> bean, BeanManagerImpl manager) {
            return manager.getContext(bean.getScope()).get(bean);
        }

        @Override
        T get(Bean<T> bean, BeanManagerImpl manager, CreationalContext<?> ctx) {
            Context context = manager.getContext(bean.getScope());
            T instance = context.get(bean);
            if (instance == null) {
                if (ctx == null) {
                    ctx = manager.createCreationalContext(bean);
                }
                instance = context.get(bean, Reflections.<CreationalContext<T>> cast(ctx));
            }
            return instance;
        }

        @Override
        void destroy(Bean<T> bean) {
            // noop
        }
    }

    private static class ApplicationScopedContextualInstanceStrategy<T> extends DefaultContextualInstanceStrategy<T> {

        private final Lock lock = new ReentrantLock();

        private volatile T value;

        @Override
        T getIfExists(Bean<T> bean, BeanManagerImpl manager) {
            T instance = value; // volatile read
            if (instance != null) {
                return instance;
            }
            lock.lock();
            try {
                if (value == null) {
                    instance = super.getIfExists(bean, manager);
                    if (instance != null) {
                        this.value = instance;
                    }
                }
                return instance;
            } finally {
                lock.unlock();
            }
        }

        @Override
        T get(Bean<T> bean, BeanManagerImpl manager, CreationalContext<?> ctx) {
            T instance = value;
            if (instance != null) {
                return instance;
            }
            lock.lock();
            try {
                if ((instance = value) == null) {
                    this.value = instance = super.get(bean, manager, ctx);
                }
                return instance;
            } finally {
                lock.unlock();
            }
        }

        @Override
        void destroy(Bean<T> bean) {
            value = null;
        }
    }

    private static class CachingContextualInstanceStrategy<T> extends DefaultContextualInstanceStrategy<T> {

        private static final Set<Class<? extends Annotation>> CACHEABLE_SCOPES = ImmutableSet.of(RequestScoped.class,
                ConversationScoped.class,
                SessionScoped.class);
        private final ThreadLocal<T> cache = new ThreadLocal<T>();

        @Override
        T getIfExists(Bean<T> bean, BeanManagerImpl manager) {
            T cached = cache.get();
            if (cached != null) {
                return cached;
            }
            cached = super.getIfExists(bean, manager);
            if (cached != null && RequestScopedCache.addItemIfActive(cache)) {
                cache.set(cached);
            }
            return cached;
        }

        @Override
        T get(Bean<T> bean, BeanManagerImpl manager, CreationalContext<?> ctx) {
            T cached = cache.get();
            if (cached != null) {
                return cached;
            }
            cached = super.get(bean, manager, ctx);
            if (RequestScopedCache.addItemIfActive(cache)) {
                cache.set(cached);
            }
            return cached;
        }
    }
}
