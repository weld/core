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
package org.jboss.weld.serialization;

import org.jboss.weld.context.SerializableContextualImpl;
import org.jboss.weld.context.SerializableContextualInstanceImpl;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.serialization.spi.helpers.SerializableContextualInstance;
import org.jboss.weld.util.reflection.Reflections;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.PassivationCapable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of {@link org.jboss.weld.serialization.spi.ContextualStore}
 *
 * @author Pete Muir
 */
public class ContextualStoreImpl implements ContextualStore {

    private static final String GENERATED_ID_PREFIX = ContextualStoreImpl.class.getName();

    // The map containing container-local contextuals
    private final ConcurrentMap<Contextual<?>, String> contextuals;
    // Inverse mapping of container-local contextuals
    private final ConcurrentMap<String, Contextual<?>> contextualsInverse;

    // The map containing passivation capable contextuals
    private final ConcurrentMap<String, Contextual<?>> passivationCapableContextuals;

    private final AtomicInteger idGenerator;

    private final String contextId;

    public ContextualStoreImpl(String contextId) {
        this.contextId = contextId;
        this.idGenerator = new AtomicInteger(0);
        this.contextuals = new ConcurrentHashMap<Contextual<?>, String>();
        this.contextualsInverse = new ConcurrentHashMap<String, Contextual<?>>();
        this.passivationCapableContextuals = new ConcurrentHashMap<String, Contextual<?>>();
    }

    /**
     * Given a particular id, return the correct contextual. For contextuals
     * which aren't passivation capable, the contextual can't be found in another
     * container, and null will be returned.
     *
     * @param id An identifier for the contextual
     * @return the contextual
     */
    @SuppressWarnings("unchecked")
    public <C extends Contextual<I>, I> C getContextual(String id) {
        if (id.startsWith(GENERATED_ID_PREFIX)) {
            return (C) contextualsInverse.get(id);
        } else {
            return (C) passivationCapableContextuals.get(id);
        }
    }

    /**
     * Add a contextual (if not already present) to the store, and return it's
     * id. If the contextual is passivation capable, it's id will be used,
     * otherwise an id will be generated
     *
     * @param contextual the contexutal to add
     * @return the current id for the contextual
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED", justification = "Using non-standard semantics of putIfAbsent")
    public String putIfAbsent(Contextual<?> contextual) {
        if (contextual instanceof PassivationCapable) {
            PassivationCapable passivationCapable = (PassivationCapable) contextual;
            String id = passivationCapable.getId();
            passivationCapableContextuals.putIfAbsent(id, contextual);
            return id;
        } else {
            String id = contextuals.get(contextual);
            if (id != null) {
                return id;
            } else {
                synchronized (contextual) {
                    id = contextuals.get(contextual);
                    if (id == null) {
                        id = new StringBuilder().append(GENERATED_ID_PREFIX).append(idGenerator.incrementAndGet()).toString();
                        contextuals.put(contextual, id);
                        contextualsInverse.put(id, contextual);
                    }
                    return id;
                }
            }
        }
    }

    public <C extends Contextual<I>, I> SerializableContextual<C, I> getSerializableContextual(Contextual<I> contextual) {
        return new SerializableContextualImpl<C, I>(contextId, Reflections.<C>cast(contextual), this);
    }

    public <C extends Contextual<I>, I> SerializableContextualInstance<C, I> getSerializableContextualInstance(Contextual<I> contextual, I instance, CreationalContext<I> creationalContext) {
        return new SerializableContextualInstanceImpl<C, I>(contextId, Reflections.<C>cast(contextual), instance, creationalContext, this);
    }

    public void cleanup() {
        contextuals.clear();
        contextualsInverse.clear();
        passivationCapableContextuals.clear();
    }
}
