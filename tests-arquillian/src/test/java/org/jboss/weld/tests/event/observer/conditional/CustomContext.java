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
package org.jboss.weld.tests.event.observer.conditional;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Vetoed;

@Vetoed
public class CustomContext implements Context {

    private static class BeanInstance {

        private final Object instance;
        @SuppressWarnings("unused")
        private final CreationalContext<?> ctx;

        public BeanInstance(Object instance, CreationalContext<?> ctx) {
            this.instance = instance;
            this.ctx = ctx;
        }

    }

    private final Map<Contextual<?>, BeanInstance> storage = new ConcurrentHashMap<Contextual<?>, BeanInstance>();

    @Override
    public Class<? extends Annotation> getScope() {
        return CustomScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        T instance = get(contextual);
        if (instance == null) {
            if (creationalContext == null) {
                throw new NullPointerException();
            }
            storage.put(contextual, new BeanInstance(contextual.create(creationalContext), creationalContext));
            instance = get(contextual);
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Contextual<T> contextual) {
        BeanInstance instance = storage.get(contextual);
        if (instance != null) {
            return (T) instance.instance;
        }
        return null;
    }

    @Override
    public boolean isActive() {
        return true;
    }

}
