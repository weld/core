/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
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
package org.jboss.weld.context.unbound;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.ProducerField;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.context.DependentContext;
import org.jboss.weld.context.SerializableContextualInstanceImpl;
import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.serialization.spi.ContextualStore;

/**
 * The dependent context
 *
 * @author Nicklas Karlsson
 */
public class DependentContextImpl implements DependentContext {

    private final ContextualStore contextualStore;

    private final String contextId;

    public DependentContextImpl(String contextId, ContextualStore contextualStore) {
        this.contextualStore = contextualStore;
        this.contextId = contextId;
    }

    /**
     * Overridden method always creating a new instance
     *
     * @param contextual The bean to create
     * @param creationalContext The creation context
     */
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        if (!isActive()) {
            throw new ContextNotActiveException();
        }
        if (creationalContext != null) {
            T instance = contextual.create(creationalContext);
            if (creationalContext instanceof WeldCreationalContext<?>) {
                addDependentInstance(instance, contextual, (WeldCreationalContext<T>) creationalContext);
            }
            return instance;
        } else {
            return null;
        }
    }

    protected <T> void addDependentInstance(T instance, Contextual<T> contextual, WeldCreationalContext<T> creationalContext) {
        // by this we are making sure that the dependent instance has no transitive dependency with @PreDestroy / disposal method
        if (creationalContext.getDependentInstances().isEmpty()) {
            if (contextual instanceof ManagedBean<?> && ! isInterceptorOrDecorator(contextual)) {
                ManagedBean<?> bean = (ManagedBean<?>) contextual;
                if (bean.getPreDestroy().isEmpty() && !bean.hasInterceptors() && bean.hasDefaultProducer()) {
                    // there is no @PreDestroy callback to call when destroying this dependent instance
                    // therefore, we do not need to keep the reference
                    return;
                }
            }
            if (contextual instanceof ProducerMethod<?, ?>) {
                ProducerMethod<?, ?> method = (ProducerMethod<?, ?>) contextual;
                if (method.getDisposalMethod() == null && method.hasDefaultProducer()) {
                    // there is no disposal method to call when destroying this dependent instance
                    // therefore, we do not need to keep the reference
                    return;
                }
            }
            if (contextual instanceof ProducerField<?, ?>) {
                ProducerField<?, ?> field = (ProducerField<?, ?>) contextual;
                if (field.hasDefaultProducer()) {
                    return;
                }
            }
        }

        // Only add the dependent instance if none of the conditions above is met
        ContextualInstance<T> beanInstance = new SerializableContextualInstanceImpl<Contextual<T>, T>(contextId, contextual, instance, creationalContext, contextualStore);
        creationalContext.addDependentInstance(beanInstance);
    }

    private boolean isInterceptorOrDecorator(Contextual<?> contextual) {
        return contextual instanceof Interceptor<?> || contextual instanceof Decorator<?>;
    }

    public <T> T get(Contextual<T> contextual) {
        return get(contextual, null);
    }

    public boolean isActive() {
        return true;
    }

    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

}
