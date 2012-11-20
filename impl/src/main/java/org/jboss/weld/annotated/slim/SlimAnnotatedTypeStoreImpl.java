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


import static org.jboss.weld.logging.messages.BootstrapMessage.DUPLICATE_ANNOTATED_TYPE_ID;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.bootstrap.api.BootstrapService;
import org.jboss.weld.exceptions.DeploymentException;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public class SlimAnnotatedTypeStoreImpl implements SlimAnnotatedTypeStore, BootstrapService {

    private final ConcurrentMap<String, SlimAnnotatedType<?>> typesById;
    private final ConcurrentMap<Class<?>, List<SlimAnnotatedType<?>>> typesByClass;

    public SlimAnnotatedTypeStoreImpl() {
        this.typesById = new ConcurrentHashMap<String, SlimAnnotatedType<?>>();
        this.typesByClass = new MapMaker().makeComputingMap(new Function<Class<?>, List<SlimAnnotatedType<?>>>() {
            @Override
            public List<SlimAnnotatedType<?>> apply(Class<?> input) {
                return new ArrayList<SlimAnnotatedType<?>>(1);
            }
        });
    }

    @Override
    public <X> SlimAnnotatedType<X> get(String id) {
        return cast(typesById.get(id));
    }

    @Override
    public <X> List<SlimAnnotatedType<X>> get(Class<X> type) {
        return cast(Collections.unmodifiableList(typesByClass.get(type)));
    }

    @Override
    public <X> void put(SlimAnnotatedType<X> type) {
        SlimAnnotatedType<?> previous = typesById.put(type.getID(), type);
        if (previous != null) {
            throw new DeploymentException(DUPLICATE_ANNOTATED_TYPE_ID, type.getID(), type, previous);
        }
        typesByClass.get(type.getJavaClass()).add(type);
    }

    @Override
    public <X> void putIfAbsent(SlimAnnotatedType<X> type) {
        Object previousValue = typesById.putIfAbsent(type.getID(), type);
        if (previousValue == null) {
            typesByClass.get(type.getJavaClass()).add(type);
        }
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
        typesByClass.clear();
    }
}
