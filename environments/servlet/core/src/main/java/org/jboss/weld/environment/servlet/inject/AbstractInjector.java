/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.servlet.inject;

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCastCacheValue;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.util.Preconditions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Provides support for Weld injection into servlets, servlet filters etc.
 *
 * @author Pete Muir
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 * @author Ales Justin
 */
public abstract class AbstractInjector {
    private final WeldManager manager;
    private final LoadingCache<Class<?>, InjectionTarget<?>> cache;

    protected AbstractInjector(final WeldManager manager) {
        Preconditions.checkArgumentNotNull(manager, "manager");
        this.manager = manager;
        this.cache = CacheBuilder.newBuilder().weakValues().build(new CacheLoader<Class<?>, InjectionTarget<?>>() {
            @Override
            public InjectionTarget<?> load(Class<?> key) throws Exception {
                return manager.createInjectionTarget(manager.createAnnotatedType(key));
            }
        });
    }

    protected void inject(Object instance) {
        final InjectionTarget<Object> it = getCastCacheValue(cache, instance.getClass());
        CreationalContext<Object> cc = manager.createCreationalContext(null);
        it.inject(instance, cc);
    }

    public void destroy(Object instance) {
        if (instance != null) {
            final InjectionTarget<Object> it = getCastCacheValue(cache, instance.getClass());
            it.dispose(instance);
        }
    }
}
