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

import static org.jboss.weld.util.reflection.Reflections.cast;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;

/**
 * Provides support for Weld injection into servlets, servlet filters etc.
 *
 * @author Pete Muir
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 * @author Ales Justin
 */
public abstract class AbstractInjector {
    private final WeldManager manager;
    private final ComputingCache<Class<?>, InjectionTarget<?>> cache;

    protected AbstractInjector(WeldManager manager) {
        Preconditions.checkArgumentNotNull(manager, "manager");
        this.manager = manager;
        this.cache = ComputingCacheBuilder.newBuilder().setWeakValues().build(clazz -> {
            AnnotatedType<?> type = manager.createAnnotatedType(clazz);
            return manager.createInjectionTargetBuilder(type)
                    .setResourceInjectionEnabled(false)
                    .setTargetClassLifecycleCallbacksEnabled(false)
                    .build();
        });
    }

    protected void inject(Object instance) {
        final InjectionTarget<Object> it = cast(cache.getValue(instance.getClass()));
        CreationalContext<Object> cc = manager.createCreationalContext(null);
        it.inject(instance, cc);
    }

    public void destroy(Object instance) {
        if (instance != null) {
            final InjectionTarget<Object> it = cast(cache.getValue(instance.getClass()));
            it.dispose(instance);
        }
    }
}
