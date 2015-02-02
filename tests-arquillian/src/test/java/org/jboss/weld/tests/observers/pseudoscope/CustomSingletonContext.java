/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.tests.observers.pseudoscope;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

/**
 * @author Kirill Gaevskii
 */
public class CustomSingletonContext implements Context {

    private final Map<Class<?>, CustomScopeInstance<?>> beans = new HashMap<Class<?>, CustomScopeInstance<?>>();

    @Override
    public Class<? extends Annotation> getScope() {
        return CustomSingletonScope.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        if (creationalContext == null) {
            return null; // simulates DeltaSpike's behavior
        }
        Bean<T> bean = (Bean<T>) contextual;
        if (beans.containsKey(bean.getBeanClass())) {
            return (T) beans.get(bean.getBeanClass()).instance;
        } else {
            T t = bean.create(creationalContext);
            CustomScopeInstance<T> customInstance = new CustomScopeInstance<T>(bean, t);
            beans.put(customInstance.bean.getBeanClass(), customInstance);
            return t;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T> T get(Contextual<T> contextual) {
        Bean<T> bean = (Bean<T>) contextual;
        if (beans.containsKey(bean.getBeanClass())) {
            return (T) beans.get(bean.getBeanClass()).instance;
        } else {
            return null;
        }
    }

    @Override
    public boolean isActive() {
        return true;
    }

    private static class CustomScopeInstance<T> {

        final Bean<T> bean;
        final T instance;

        public CustomScopeInstance(Bean<T> bean, T instance) {
            this.bean = bean;
            this.instance = instance;
        }
    }
}
