/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.contexts.unbound;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Interceptor;

import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.context.DependentContext;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.contexts.SerializableContextualInstanceImpl;
import org.jboss.weld.contexts.WeldCreationalContext;
import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.injection.producer.AbstractMemberProducer;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.serialization.spi.ContextualStore;

/**
 * The dependent context
 *
 * @author Nicklas Karlsson
 */
public class DependentContextImpl implements DependentContext {

    private final ContextualStore contextualStore;

    public DependentContextImpl(ContextualStore contextualStore) {
        this.contextualStore = contextualStore;
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
            if (contextual instanceof ManagedBean<?> && !isInterceptorOrDecorator(contextual)) {
                ManagedBean<?> managedBean = (ManagedBean<?>) contextual;
                if (managedBean.getProducer() instanceof BasicInjectionTarget<?>) {
                    BasicInjectionTarget<?> injectionTarget = (BasicInjectionTarget<?>) managedBean.getProducer();
                    if (!injectionTarget.getLifecycleCallbackInvoker().hasPreDestroyMethods()
                            && !injectionTarget.hasInterceptors()) {
                        // there is no @PreDestroy callback to call when destroying this dependent instance
                        // therefore, we do not need to keep the reference
                        return;
                    }
                }
            }
            if (contextual instanceof AbstractProducerBean<?, ?, ?>) {
                AbstractProducerBean<?, ?, ?> producerBean = (AbstractProducerBean<?, ?, ?>) contextual;
                if (producerBean.getProducer() instanceof AbstractMemberProducer<?, ?>) {
                    AbstractMemberProducer<?, ?> producer = (AbstractMemberProducer<?, ?>) producerBean.getProducer();
                    if (producer.getDisposalMethod() == null) {
                        // there is no disposal method to call when destroying this dependent instance
                        // therefore, we do not need to keep the reference
                        return;
                    }
                }
            }
            if (isOptimizableBuiltInBean(contextual)) {
                // Most built-in dependent beans do not have to be stored
                return;
            }
        }

        // Only add the dependent instance if none of the conditions above is met
        ContextualInstance<T> beanInstance = new SerializableContextualInstanceImpl<Contextual<T>, T>(contextual, instance,
                creationalContext, contextualStore);
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

    @Override
    public void destroy(Contextual<?> contextual) {
        throw new UnsupportedOperationException();
    }

    private boolean isOptimizableBuiltInBean(Contextual<?> contextual) {
        if (contextual instanceof AbstractBuiltInBean<?>) {
            AbstractBuiltInBean<?> abstractBuiltInBean = (AbstractBuiltInBean<?>) contextual;
            return abstractBuiltInBean.isDependentContextOptimizationAllowed();
        }
        return false;
    }
}
