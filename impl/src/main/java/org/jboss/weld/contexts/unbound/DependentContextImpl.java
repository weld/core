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
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
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
            // handle managed beans
            if (contextual instanceof ManagedBean<?> managedBean) {
                if (contextual instanceof Interceptor<?>) {
                    // Interceptors cannot have pre-destroy callbacks
                    // At this point we know the interceptor has no dependent instances, so we needn't keep its reference
                    // NOTE: decorators CAN have @PreDestroy callbacks!
                    return;
                }
                if (managedBean.getProducer() instanceof BasicInjectionTarget<?> injectionTarget) {
                    boolean hasPreDestroyMethods = injectionTarget.getLifecycleCallbackInvoker().hasPreDestroyMethods();
                    boolean hasPreDestroyInt = hasPreDestroyInterceptor(managedBean);
                    if (!hasPreDestroyMethods && !hasPreDestroyInt) {
                        // there is no @PreDestroy callback to call when destroying this dependent instance
                        // therefore, we do not need to keep the reference
                        // Note that we need to account for @PreDestroy on the bean as well as interceptors
                        return;
                    }
                }
            }
            if (contextual instanceof AbstractProducerBean<?, ?, ?> producerBean) {
                if (producerBean.getProducer() instanceof AbstractMemberProducer<?, ?> producer) {
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

    /**
     * Checks if the managed bean has any interceptors with @PreDestroy lifecycle callbacks.
     * Beans with only @AroundInvoke or other non-lifecycle interceptors should not be tracked.
     *
     * @param managedBean the managed bean to check
     * @return true if the bean has @PreDestroy interceptors, false otherwise
     */
    private boolean hasPreDestroyInterceptor(ManagedBean<?> managedBean) {
        InterceptionModel interceptionModel = managedBean.getBeanManager().getInterceptorModelRegistry()
                .get(managedBean.getAnnotated());
        if (interceptionModel != null) {
            // Check if there are any external PRE_DESTROY interceptors (method is null for lifecycle interceptors)
            if (!interceptionModel.getInterceptors(InterceptionType.PRE_DESTROY, null).isEmpty()) {
                return true;
            }
            // Check if the bean class itself has @PreDestroy interceptor methods
            if (interceptionModel.hasTargetClassInterceptors()) {
                return interceptionModel.getTargetClassInterceptorMetadata().isEligible(InterceptionType.PRE_DESTROY);
            }
        }
        return false;
    }
}
