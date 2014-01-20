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

import java.util.Collection;

import org.jboss.weld.bootstrap.api.BootstrapService;

/**
 * Stores {@link SlimAnnotatedType}s.
 *
 * @author Jozef Hartinger
 *
 */
public interface SlimAnnotatedTypeStore extends BootstrapService {

    /**
     * Retrieves a previously stored list of {@link SlimAnnotatedType}s that match the given type.
     *
     * @param type the annotated type type
     * @return
     */
    <X> Collection<SlimAnnotatedType<X>> get(Class<X> type);

    /**
     * Retrieves a previously stored {@link SlimAnnotatedType} that matches the given type and ID suffix.
     */
    <X> SlimAnnotatedType<X> get(Class<X> type, String suffix);

    /**
     * Put a {@link SlimAnnotatedType} into the store.
     *
     * @param type type to store
     */
    <X> void put(SlimAnnotatedType<X> type);

}
