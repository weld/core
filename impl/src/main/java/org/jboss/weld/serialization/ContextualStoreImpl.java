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

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.bean.CommonBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.contexts.SerializableContextualFactory;
import org.jboss.weld.contexts.SerializableContextualInstanceImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.serialization.spi.helpers.SerializableContextualInstance;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Implementation of {@link org.jboss.weld.serialization.spi.ContextualStore}
 *
 * @author Pete Muir
 */
public class ContextualStoreImpl implements ContextualStore {

    private static final String GENERATED_ID_PREFIX = ContextualStoreImpl.class.getName();

    // The map containing container-local contextuals
    private final ConcurrentMap<Contextual<?>, BeanIdentifier> contextuals;
    // Inverse mapping of container-local contextuals
    private final ConcurrentMap<BeanIdentifier, Contextual<?>> contextualsInverse;

    // The map containing passivation capable contextuals
    private final ConcurrentMap<BeanIdentifier, Contextual<?>> passivationCapableContextuals;

    private final AtomicInteger idGenerator;

    private final String contextId;

    private final BeanIdentifierIndex beanIdentifierIndex;

    public ContextualStoreImpl(String contextId, BeanIdentifierIndex beanIdentifierIndex) {
        this.contextId = contextId;
        this.beanIdentifierIndex = beanIdentifierIndex;
        this.idGenerator = new AtomicInteger(0);
        this.contextuals = new ConcurrentHashMap<Contextual<?>, BeanIdentifier>();
        this.contextualsInverse = new ConcurrentHashMap<BeanIdentifier, Contextual<?>>();
        this.passivationCapableContextuals = new ConcurrentHashMap<BeanIdentifier, Contextual<?>>();
    }

    /**
     * Given a particular id, return the correct contextual. For contextuals
     * which aren't passivation capable, the contextual can't be found in another
     * container, and null will be returned.
     *
     * @param id An identifier for the contextual
     * @return the contextual
     */
    public <C extends Contextual<I>, I> C getContextual(String id) {
        return this.<C, I> getContextual(new StringBeanIdentifier(id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends Contextual<I>, I> C getContextual(BeanIdentifier identifier) {
        if (identifier.asString().startsWith(GENERATED_ID_PREFIX)) {
            return (C) contextualsInverse.get(identifier);
        } else {
            return (C) passivationCapableContextuals.get(identifier);
        }
    }

    /**
     * Add a contextual (if not already present) to the store, and return it's
     * id. If the contextual is passivation capable, it's id will be used,
     * otherwise an id will be generated
     *
     * @param contextual the contextual to add
     * @return the current id for the contextual
     */
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED", justification = "Using non-standard semantics of putIfAbsent")
    public BeanIdentifier putIfAbsent(Contextual<?> contextual) {
        if (contextual instanceof CommonBean<?>) {
            // this is a Bean<?> created by Weld
            CommonBean<?> bean = (CommonBean<?>) contextual;
            passivationCapableContextuals.putIfAbsent(bean.getIdentifier(), contextual);
            return bean.getIdentifier();
        }
        if (contextual instanceof PassivationCapable) {
            // this is an extension-provided passivation capable bean
            PassivationCapable passivationCapable = (PassivationCapable) contextual;
            String id = passivationCapable.getId();
            BeanIdentifier identifier = new StringBeanIdentifier(id);
            passivationCapableContextuals.putIfAbsent(identifier, contextual);
            return identifier;
        } else {
            BeanIdentifier id = contextuals.get(contextual);
            if (id != null) {
                return id;
            } else {
                synchronized (contextual) {
                    id = contextuals.get(contextual);
                    if (id == null) {
                        id = new StringBeanIdentifier(new StringBuilder().append(GENERATED_ID_PREFIX)
                                .append(idGenerator.incrementAndGet()).toString());
                        contextuals.put(contextual, id);
                        contextualsInverse.put(id, contextual);
                    }
                    return id;
                }
            }
        }
    }

    public <C extends Contextual<I>, I> SerializableContextual<C, I> getSerializableContextual(Contextual<I> contextual) {
        if (contextual instanceof SerializableContextual<?, ?>) {
            return cast(contextual);
        }
        return SerializableContextualFactory.create(contextId, Reflections.<C> cast(contextual), this, beanIdentifierIndex);
    }

    public <C extends Contextual<I>, I> SerializableContextualInstance<C, I> getSerializableContextualInstance(
            Contextual<I> contextual, I instance, CreationalContext<I> creationalContext) {
        return new SerializableContextualInstanceImpl<C, I>(Reflections.<C> cast(contextual), instance, creationalContext,
                this);
    }

    public void cleanup() {
        contextuals.clear();
        contextualsInverse.clear();
        passivationCapableContextuals.clear();
    }

    public void removeAll(Iterable<Bean<?>> removable) {
        for (Bean<?> bean : removable) {
            BeanIdentifier beanIdentifier = contextuals.remove(bean);
            if (beanIdentifier == null && bean instanceof RIBean) {
                beanIdentifier = ((RIBean<?>) bean).getIdentifier();
            }
            if (beanIdentifier != null) {
                contextualsInverse.remove(beanIdentifier);
                passivationCapableContextuals.remove(beanIdentifier);
            }
        }
    }
}
