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

import java.util.Collection;

import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.context.WeldAlterableContext;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.ForwardingContext;

/**
 * Wraps a passivating context with a wrapper that guarantees that each call to the context is done with serializable
 * {@link Contextual}. The wrapper uses {@link SerializableContextual} if necessary.
 *
 * @author Jozef Hartinger
 *
 */
public class PassivatingContextWrapper {

    private PassivatingContextWrapper() {
    }

    public static Context wrap(Context context, ContextualStore store) {
        if (context instanceof WeldAlterableContext) {
            return new WeldAlterableContextWrapper((WeldAlterableContext) context, store);
        } else {
            if (context instanceof AlterableContext) {
                return new AlterableContextWrapper((AlterableContext) context, store);
            } else {
                return new ContextWrapper(context, store);
            }
        }
    }

    public static Context unwrap(Context context) {
        if (context instanceof AbstractPassivatingContextWrapper<?>) {
            AbstractPassivatingContextWrapper<?> wrapper = (AbstractPassivatingContextWrapper<?>) context;
            return wrapper.delegate();
        } else {
            return context;
        }
    }

    private abstract static class AbstractPassivatingContextWrapper<C extends Context> extends ForwardingContext {

        private final C context;
        protected final ContextualStore store;

        public AbstractPassivatingContextWrapper(C context, ContextualStore store) {
            this.context = context;
            this.store = store;
        }

        @Override
        public <T> T get(Contextual<T> contextual) {
            contextual = store.getSerializableContextual(contextual);
            return context.get(contextual);
        }

        @Override
        public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
            contextual = store.getSerializableContextual(contextual);
            return context.get(contextual, creationalContext);
        }

        @Override
        protected C delegate() {
            return context;
        }
    }

    private static class ContextWrapper extends AbstractPassivatingContextWrapper<Context> {

        public ContextWrapper(Context context, ContextualStore store) {
            super(context, store);
        }
    }

    private static class AlterableContextWrapper extends AbstractPassivatingContextWrapper<AlterableContext>
            implements AlterableContext {

        public AlterableContextWrapper(AlterableContext context, ContextualStore store) {
            super(context, store);
        }

        @Override
        public void destroy(Contextual<?> contextual) {
            contextual = store.getSerializableContextual(contextual);
            delegate().destroy(contextual);
        }
    }

    private static class WeldAlterableContextWrapper extends AbstractPassivatingContextWrapper<WeldAlterableContext>
            implements WeldAlterableContext {

        public WeldAlterableContextWrapper(WeldAlterableContext context, ContextualStore store) {
            super(context, store);
        }

        @Override
        public void destroy(Contextual<?> contextual) {
            contextual = store.getSerializableContextual(contextual);
            delegate().destroy(contextual);
        }

        @Override
        public Collection<ContextualInstance<?>> getAllContextualInstances() {
            return delegate().getAllContextualInstances();
        }

        @Override
        public void clearAndSet(Collection<ContextualInstance<?>> setOfInstances) {
            delegate().clearAndSet(setOfInstances);
        }
    }
}
