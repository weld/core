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

import org.jboss.weld.bootstrap.api.BootstrapService;
import org.jboss.weld.exceptions.DeploymentException;

import static org.jboss.weld.logging.messages.BootstrapMessage.DUPLICATE_ANNOTATED_TYPE_ID;;

public class SlimAnnotatedTypeStoreImpl implements SlimAnnotatedTypeStore, BootstrapService {

    private final ConcurrentMap<String, SlimAnnotatedType<?>> typesById;

    public SlimAnnotatedTypeStoreImpl() {
        this.typesById = new ConcurrentHashMap<String, SlimAnnotatedType<?>>();
    }

    @Override
    public <X> SlimAnnotatedType<X> get(String id) {
        return cast(typesById.get(id));
    }

    @Override
    public <X> void put(SlimAnnotatedType<X> type) {
        SlimAnnotatedType<?> previous = typesById.put(type.getID(), type);
        if (previous != null) {
            throw new DeploymentException(DUPLICATE_ANNOTATED_TYPE_ID, type.getID(), type, previous);
        }
    }

    @Override
    public <X> void putIfAbsent(SlimAnnotatedType<X> type) {
        typesById.putIfAbsent(type.getID(), type);
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
