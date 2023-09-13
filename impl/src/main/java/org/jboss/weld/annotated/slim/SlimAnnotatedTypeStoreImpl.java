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

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jboss.weld.bootstrap.api.helpers.AbstractBootstrapService;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.collections.WeldCollections;

public class SlimAnnotatedTypeStoreImpl extends AbstractBootstrapService implements SlimAnnotatedTypeStore {

    private final ComputingCache<Class<?>, Set<SlimAnnotatedType<?>>> typesByClass;

    public SlimAnnotatedTypeStoreImpl() {
        this.typesByClass = ComputingCacheBuilder.newBuilder().build((x) -> new CopyOnWriteArraySet<SlimAnnotatedType<?>>());
    }

    @Override
    public <X> SlimAnnotatedType<X> get(Class<X> type, String suffix) {
        for (SlimAnnotatedType<X> annotatedType : get(type)) {
            if (Objects.equals(annotatedType.getIdentifier().getSuffix(), suffix)) {
                return annotatedType;
            }
        }
        return null;
    }

    @Override
    public <X> Set<SlimAnnotatedType<X>> get(Class<X> type) {
        return WeldCollections.immutableSetView(typesByClass.getCastValue(type));
    }

    @Override
    public <X> void put(SlimAnnotatedType<X> type) {
        typesByClass.getValue(type.getJavaClass()).add(type);
    }

    @Override
    public void cleanupAfterBoot() {
        typesByClass.clear();
    }

}
