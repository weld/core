/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.passivating.custom;

import static org.jboss.weld.util.reflection.Reflections.cast;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

public class ClusterContext implements Context {

    private final Map<Contextual<?>, Object> instances = new HashMap<Contextual<?>, Object>();

    private boolean called;

    @Override
    public Class<? extends Annotation> getScope() {
        return ClusterScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> ctx) {
        called = true;
        assertTrue(contextual instanceof Serializable);

        // WELD-1537
        assertTrue(contextual instanceof Bean);

        if (ctx != null) {
            assertTrue(ctx instanceof Serializable);
        }
        synchronized (instances) {
            T instance = cast(instances.get(contextual));
            if (instance == null && ctx != null) {
                instances.put(contextual, contextual.create(ctx));
                instance = cast(instances.get(contextual));
            }
            return instance;
        }
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        return get(contextual, null);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    public boolean isCalled() {
        return called;
    }

    public void reset() {
        called = false;
    }
}
