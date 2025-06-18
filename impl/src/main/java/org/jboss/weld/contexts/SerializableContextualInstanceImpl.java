/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.contexts;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.serialization.spi.helpers.SerializableContextualInstance;

public class SerializableContextualInstanceImpl<C extends Contextual<I>, I> implements SerializableContextualInstance<C, I> {

    private static final long serialVersionUID = -6366271037267396256L;

    private final SerializableContextual<C, I> contextual;
    private final I instance;
    private final CreationalContext<I> creationalContext;

    public SerializableContextualInstanceImpl(C contextual, I instance, CreationalContext<I> creationalContext,
            ContextualStore contextualStore) {
        this.contextual = contextualStore.getSerializableContextual(contextual);
        this.instance = instance;
        this.creationalContext = creationalContext;
    }

    public SerializableContextualInstanceImpl(SerializableContextual<C, I> contextual, I instance,
            CreationalContext<I> creationalContext) {
        this.contextual = contextual;
        this.instance = instance;
        this.creationalContext = creationalContext;
    }

    public SerializableContextual<C, I> getContextual() {
        return contextual;
    }

    public I getInstance() {
        return instance;
    }

    public CreationalContext<I> getCreationalContext() {
        return creationalContext;
    }

    @Override
    public String toString() {
        return "Bean: " + contextual + "; Instance: " + instance.getClass().getName() + "@"
                + Integer.toHexString(instance.hashCode()) + "; CreationalContext: " + creationalContext;
    }

}
