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
package org.jboss.weld.annotated.slim;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.bootstrap.api.BootstrapService;
import org.jboss.weld.resources.ReflectionCache;
import org.jboss.weld.resources.SharedObjectCache;

public class SlimAnnotatedTypeStoreImpl implements SlimAnnotatedTypeStore, BootstrapService {

    private final ConcurrentMap<String, SlimAnnotatedType<?>> typesById;
    private final SharedObjectCache sharedObjectCache;
    private final ReflectionCache reflectionCache;

    public SlimAnnotatedTypeStoreImpl(SharedObjectCache sharedObjectCache, ReflectionCache reflectionCache) {
        this.typesById = new ConcurrentHashMap<String, SlimAnnotatedType<?>>();
        this.sharedObjectCache = sharedObjectCache;
        this.reflectionCache = reflectionCache;
    }

    @Override
    public <X> SlimAnnotatedType<X> create(Class<X> javaClass) {
        return BackedAnnotatedType.of(javaClass, sharedObjectCache, reflectionCache);
    }

    @Override
    public <X> SlimAnnotatedType<X> get(String id) {
        return cast(typesById.get(id));
    }

    @Override
    public <X> void put(SlimAnnotatedType<X> type) {
        typesById.put(type.getID(), type);
        // TODO handle duplicates
    }

    @Override
    public void cleanupAfterBoot() {
        for (SlimAnnotatedType<?> type : typesById.values()) {
            type.clear();
        }
    }

    @Override
    public void cleanup() {
        typesById.clear();
    }
}
