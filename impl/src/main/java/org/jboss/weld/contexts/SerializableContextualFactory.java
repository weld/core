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
package org.jboss.weld.contexts;

import java.io.Serializable;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.Container;
import org.jboss.weld.bean.ForwardingBean;
import org.jboss.weld.bean.WrappedContextual;
import org.jboss.weld.serialization.BeanIdentifierIndex;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Produces wrappers for {@link Contextual}s which are serializable.
 *
 * @author Jozef Hartinger
 * @author Martin Kouba
 */
public class SerializableContextualFactory {

    private SerializableContextualFactory() {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <C extends Contextual<I>, I> SerializableContextual<C, I> create(String contextId, C contextual,
            ContextualStore contextualStore,
            BeanIdentifierIndex beanIdentifierIndex) {
        if (contextual instanceof Bean) {
            if (contextual instanceof PassivationCapable) {
                return new PassivationCapableSerializableBean(contextId, Reflections.<Bean> cast(contextual), contextualStore,
                        beanIdentifierIndex);
            } else {
                return new DefaultSerializableBean(contextId, Reflections.<Bean> cast(contextual), contextualStore,
                        beanIdentifierIndex);
            }
        } else {
            if (contextual instanceof PassivationCapable) {
                return new PassivationCapableSerializableContextual(contextId, contextual, contextualStore,
                        beanIdentifierIndex);
            } else {
                return new DefaultSerializableContextual<C, I>(contextId, contextual, contextualStore, beanIdentifierIndex);
            }
        }
    }

    private static final class SerializableContextualHolder<C extends Contextual<I>, I> implements Serializable {

        private static final long serialVersionUID = 46941665668478370L;

        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "A cache which is lazily loaded")
        // A cached, transient version of the contextual
        private transient C cached;

        // Only one of the three fields is used at the same time - directly serializable contextual, bean identifier or index
        private final C serializable;
        private final BeanIdentifier identifier;
        private final Integer identifierIndex;

        private final String contextId;

        private transient ContextualStore cachedContextualStore;

        private transient BeanIdentifierIndex beanIdentifierIndex;

        SerializableContextualHolder(String contextId, C contextual, ContextualStore contextualStore,
                BeanIdentifierIndex beanIdentifierIndex) {
            this.contextId = contextId;
            this.cachedContextualStore = contextualStore;
            if (contextual instanceof Serializable) {
                // the contextual is serializable, so we can just use it
                this.serializable = contextual;
                this.identifier = null;
                this.identifierIndex = null;
            } else {
                this.serializable = null;
                BeanIdentifier beanIdentifier = getId(contextual, contextualStore);
                // The index may be null or not built yet
                Integer idx = null;
                if (beanIdentifierIndex != null && beanIdentifierIndex.isBuilt()) {
                    idx = beanIdentifierIndex.getIndex(beanIdentifier);
                }
                if (idx != null) {
                    this.identifierIndex = idx;
                    this.identifier = null;
                } else {
                    this.identifierIndex = null;
                    this.identifier = beanIdentifier;
                }
            }
            // cache the contextual
            this.cached = contextual;
        }

        protected BeanIdentifier getId(C contextual, ContextualStore contextualStore) {
            return Beans.getIdentifier(contextual, contextualStore);
        }

        protected ContextualStore getContextualStore() {
            if (cachedContextualStore == null) {
                this.cachedContextualStore = Container.instance(contextId).services().get(ContextualStore.class);
            }
            return this.cachedContextualStore;
        }

        protected BeanIdentifierIndex getBeanIdentifierIndex() {
            if (beanIdentifierIndex == null) {
                beanIdentifierIndex = Container.instance(contextId).services().get(BeanIdentifierIndex.class);
            }
            return beanIdentifierIndex;
        }

        protected C get() {
            if (cached == null) {
                loadContextual();
            }
            return cached;
        }

        private void loadContextual() {
            if (serializable != null) {
                cached = serializable;
            } else if (identifierIndex != null) {
                cached = getContextualStore().<C, I> getContextual(getBeanIdentifierIndex().getIdentifier(identifierIndex));
            } else if (identifier != null) {
                cached = getContextualStore().<C, I> getContextual(identifier);
            }
            if (cached == null) {
                throw new IllegalStateException("Error restoring serialized contextual with id " + identifier);
            }
        }

    }

    private abstract static class AbstractSerializableBean<B extends Bean<I>, I> extends ForwardingBean<I>
            implements SerializableContextual<B, I>, WrappedContextual<I> {

        private static final long serialVersionUID = 7594992948498685840L;

        private final SerializableContextualHolder<B, I> holder;

        AbstractSerializableBean(String contextId, B bean, ContextualStore contextualStore,
                BeanIdentifierIndex beanIdentifierIndex) {
            this.holder = new SerializableContextualHolder<B, I>(contextId, bean, contextualStore, beanIdentifierIndex);
        }

        @Override
        public B get() {
            return holder.get();
        }

        @Override
        public Bean<I> delegate() {
            return get();
        }

        @Override
        public boolean equals(Object obj) {
            // if the arriving object is also a AbstractSerializableBean, then unwrap it
            if (obj instanceof AbstractSerializableBean<?, ?>) {
                return delegate().equals(((AbstractSerializableBean<?, ?>) obj).get());
            } else {
                return delegate().equals(obj);
            }
        }

        @Override
        public int hashCode() {
            return delegate().hashCode();
        }

    }

    private abstract static class AbstractSerializableContextual<C extends Contextual<I>, I> extends ForwardingContextual<I>
            implements
            SerializableContextual<C, I>, WrappedContextual<I> {

        private static final long serialVersionUID = 107855630671709443L;

        private final SerializableContextualHolder<C, I> holder;

        AbstractSerializableContextual(String contextId, C contextual, ContextualStore contextualStore,
                BeanIdentifierIndex beanIdentifierIndex) {
            this.holder = new SerializableContextualHolder<C, I>(contextId, contextual, contextualStore, beanIdentifierIndex);
        }

        @Override
        public Contextual<I> delegate() {
            return get();
        }

        public C get() {
            return holder.get();
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
    private static class DefaultSerializableContextual<C extends Contextual<I>, I>
            extends AbstractSerializableContextual<C, I> {

        private static final long serialVersionUID = -5102624795925717767L;

        public DefaultSerializableContextual(String contextId, C contextual, ContextualStore contextualStore,
                BeanIdentifierIndex beanIdentifierIndex) {
            super(contextId, contextual, contextualStore, beanIdentifierIndex);
        }
    }

    // every Contextual with passivating scope should implement PassivationCapable
    private static class PassivationCapableSerializableContextual<C extends Contextual<I> & PassivationCapable, I>
            extends AbstractSerializableContextual<C, I>
            implements PassivationCapable {

        private static final long serialVersionUID = -2753893863961869301L;

        public PassivationCapableSerializableContextual(String contextId, C contextual, ContextualStore contextualStore,
                BeanIdentifierIndex beanIdentifierIndex) {
            super(contextId, contextual, contextualStore, beanIdentifierIndex);
        }

        @Override
        public String getId() {
            return get().getId();
        }
    }

    private static class DefaultSerializableBean<B extends Bean<I>, I> extends AbstractSerializableBean<B, I> {

        private static final long serialVersionUID = -8901252027789701049L;

        public DefaultSerializableBean(String contextId, B bean, ContextualStore contextualStore,
                BeanIdentifierIndex beanIdentifierIndex) {
            super(contextId, bean, contextualStore, beanIdentifierIndex);
        }
    }

    private static class PassivationCapableSerializableBean<B extends Bean<I> & PassivationCapable, I>
            extends AbstractSerializableBean<B, I> implements
            PassivationCapable {

        private static final long serialVersionUID = 7458443513156329183L;

        public PassivationCapableSerializableBean(String contextId, B bean, ContextualStore contextualStore,
                BeanIdentifierIndex beanIdentifierIndex) {
            super(contextId, bean, contextualStore, beanIdentifierIndex);
        }

        @Override
        public String getId() {
            return get().getId();
        }
    }
}
