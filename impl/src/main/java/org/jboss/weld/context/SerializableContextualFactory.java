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
package org.jboss.weld.context;

import java.io.Serializable;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.Container;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Produces wrappers for {@link Contextual}s which are serializable.
 *
 * @author Jozef Hartinger
 *
 */
public class SerializableContextualFactory {

    private SerializableContextualFactory() {
    }

    @java.lang.SuppressWarnings({ "rawtypes", "unchecked" })
    public static <C extends Contextual<I>, I> SerializableContextual<C, I> create(C contextual, ContextualStore contextualStore) {
        if (contextual instanceof PassivationCapable) {
            return new PassivationCapableSerializableContextual(contextual, contextualStore);
        } else {
            return new DefaultSerializableContextual<C, I>(contextual, contextualStore);
        }
    }

    private abstract static class AbstractSerializableContextual<C extends Contextual<I>, I> extends ForwardingContextual<I> implements SerializableContextual<C, I> {

        private static final long serialVersionUID = 107855630671709443L;

        @Override
        protected Contextual<I> delegate() {
            return get();
        }

        // A directly serializable contextual
        private C serialiazable;
        @SuppressWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "A cache which is lazily loaded")
        // A cached, transient version of the contextual
        private transient C cached;

        // the id of a non-serializable, passivation capable contextual
        private String id;

        private transient ContextualStore cachedContextualStore;

        public AbstractSerializableContextual(C contextual, ContextualStore contextualStore) {
            this.cachedContextualStore = contextualStore;
            if (contextual instanceof Serializable) {
                // the contextual is serializable, so we can just use it
                this.serialiazable = contextual;
            } else {
                this.id = getId(contextual, contextualStore);
            }

            // cache the contextual
            this.cached = contextual;
        }

        protected abstract String getId(C contextual, ContextualStore contextualStore);

        private ContextualStore getContextualStore() {
            if (cachedContextualStore == null) {
                this.cachedContextualStore = Container.instance().services().get(ContextualStore.class);
            }
            return this.cachedContextualStore;
        }

        public C get() {
            if (cached == null) {
                loadContextual();
            }
            return cached;
        }

        private void loadContextual() {
            if (serialiazable != null) {
                this.cached = serialiazable;
            } else if (id != null) {
                this.cached = getContextualStore().<C, I> getContextual(id);
            }
            if (this.cached == null) {
                throw new IllegalStateException("Error restoring serialized contextual with id " + id);
            }
        }

        @Override
        public boolean equals(Object obj) {
            // if the arriving object is also a AbstractSerializableContextual, then unwrap it
            if (obj instanceof AbstractSerializableContextual<?, ?>) {
                return delegate().equals(((AbstractSerializableContextual<?, ?>) obj).get());
            } else {
                return delegate().equals(obj);
            }
        }

        @Override
        public int hashCode() {
            return delegate().hashCode();
        }
    }

    // for Contextuals that are not PassivationCapable - bean id is generated (may not be portable between container instances)
    private static class DefaultSerializableContextual<C extends Contextual<I>, I> extends AbstractSerializableContextual<C, I> {

        private static final long serialVersionUID = -5102624795925717767L;

        public DefaultSerializableContextual(C contextual, ContextualStore contextualStore) {
            super(contextual, contextualStore);
        }

        @Override
        protected String getId(C contextual, ContextualStore contextualStore) {
            return contextualStore.putIfAbsent(contextual);
        }

    }

    // every Contextual with passivating scope should implement PassivationCapable
    private static class PassivationCapableSerializableContextual<C extends Contextual<I> & PassivationCapable, I> extends AbstractSerializableContextual<C, I> implements PassivationCapable {

        private static final long serialVersionUID = -2753893863961869301L;

        public PassivationCapableSerializableContextual(C contextual, ContextualStore contextualStore) {
            super(contextual, contextualStore);
        }

        @Override
        protected String getId(C contextual, ContextualStore contextualStore) {
            return contextual.getId();
        }

        @Override
        public String getId() {
            return get().getId();
        }
    }
}
