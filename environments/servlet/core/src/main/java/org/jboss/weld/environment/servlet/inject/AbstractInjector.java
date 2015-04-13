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

import java.util.Map;
import java.util.WeakHashMap;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.util.Preconditions;

/**
 * Provides support for Weld injection into servlets, servlet filters etc.
 *
 * @author Pete Muir
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 * @author Ales Justin
 */
public abstract class AbstractInjector {
    private final WeldManager manager;
    private final Map<Class<?>, InjectionTarget<?>> cache = new WeakHashMap<Class<?>, InjectionTarget<?>>();

    protected AbstractInjector(WeldManager manager) {
        Preconditions.checkArgumentNotNull(manager, "manager");
        this.manager = manager;
    }

    protected void inject(Object instance) {
        // not data-race safe, however doesn't matter, as the injection target created for class A is interchangeable for another injection target created for class A
        // TODO Make this a concurrent cache when we switch to google collections
        Class<?> clazz = instance.getClass();
        if (!cache.containsKey(clazz)) {
            cache.put(clazz, manager.createInjectionTarget(manager.createAnnotatedType(clazz)));
        }
        CreationalContext<Object> cc = manager.createCreationalContext(null);
        InjectionTarget<Object> it = (InjectionTarget<Object>) cache.get(clazz);
        it.inject(instance, cc);
    }

    public void destroy(Object instance) {
        if (instance != null) {
            AnnotatedType type = manager.createAnnotatedType(instance.getClass());
            InjectionTarget it = manager.createInjectionTarget(type);
            it.dispose(instance);
        }
    }
}
