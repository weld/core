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

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.exceptions.DeploymentException;

/**
 * Store for {@link SlimAnnotatedType}s. This service keeps references to {@link SlimAnnotatedType}s mapped by their ids. The
 * service is used for serialization of {@link SlimAnnotatedType} and lookup.
 *
 * @author Jozef Hartinger
 *
 */
public interface SlimAnnotatedTypeStore extends Service {

    /**
     * Retrieves a previously stored {@link SlimAnnotatedType}.
     *
     * @param id type identifier
     * @return type identified by the identifier
     */
    <X> SlimAnnotatedType<X> get(String id);

    /**
     * Put a {@link SlimAnnotatedType} into the store.
     *
     * @param type type to store
     * @throws DeploymentException if the type identifier is not unique
     */
    <X> void put(SlimAnnotatedType<X> type);

    /**
     * Put a {@link SlimAnnotatedType} into the store. Nothing is done if the store already contains a type with the given id.
     *
     * @param type type to store
     */
    <X> void putIfAbsent(SlimAnnotatedType<X> type);
}
