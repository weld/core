/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.weld.bean.proxy;

import org.jboss.weld.Container;
import org.jboss.weld.context.CreationalContextImpl;
import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.EmptyInjectionPoint;
import org.jboss.weld.serialization.spi.ContextualStore;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import java.io.Serializable;

import static org.jboss.weld.util.reflection.Reflections.cast;

/**
 * An instance locator that uses a context to lookup the instance if
 * it exists; otherwise, a new instance will be created from the
 * bean.
 *
 * @author David Allen
 */
public class ContextBeanInstance<T> extends AbstractBeanInstance implements Serializable {

    private static final long serialVersionUID = -8144230657830556503L;
    // The bean
    private transient Bean<T> bean;
    // The bean index in the manager
    private final String id;

    private final String contextId;
    // The actual type of the resulting bean instance
    private final Class<?> instanceType;

    private static final ThreadLocal<WeldCreationalContext<?>> currentCreationalContext = new ThreadLocal<WeldCreationalContext<?>>();


    /**
     * Creates a new locator for instances of the given bean.
     *
     * @param bean The contextual bean
     * @param id   The unique identifier of this bean
     */
    public ContextBeanInstance(Bean<T> bean, String id, String contextId) {
        this.bean = bean;
        this.id = id;
        this.contextId = contextId;
        this.instanceType = computeInstanceType(bean);
        log.trace("Created context instance locator for bean " + bean + " identified as " + id);
    }

    public T getInstance() {
        Container container = Container.instance(contextId);
        if (bean == null) {
            bean = container.services().get(ContextualStore.class).<Bean<T>, T>getContextual(id);
        }
        Context context = container.deploymentManager().getContext(bean.getScope());

        T existingInstance = context.get(bean);
        if (existingInstance != null) {
            return existingInstance;
        }

        WeldCreationalContext<T> creationalContext;
        WeldCreationalContext<?> previousCreationalContext = currentCreationalContext.get();
        if (currentCreationalContext.get() == null) {
            creationalContext = new CreationalContextImpl<T>(bean);
        } else {
            creationalContext = currentCreationalContext.get().getCreationalContext(bean);
        }
        final CurrentInjectionPoint currentInjectionPoint = container.services().get(CurrentInjectionPoint.class);
        currentCreationalContext.set(creationalContext);
        try {
            // Ensure that there is no injection point associated
            currentInjectionPoint.push(EmptyInjectionPoint.INSTANCE);
            return context.get(bean, creationalContext);
        } finally {
            currentInjectionPoint.pop();
            if (previousCreationalContext == null) {
                currentCreationalContext.remove();
            } else {
                currentCreationalContext.set(previousCreationalContext);
            }
        }
    }

    public Class<T> getInstanceType() {
        return cast(instanceType);
    }

}
