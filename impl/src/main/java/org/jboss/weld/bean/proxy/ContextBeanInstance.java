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

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectStreamException;
import java.io.Serializable;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.Container;
import org.jboss.weld.bean.ContextualInstance;
import org.jboss.weld.contexts.CreationalContextImpl;
import org.jboss.weld.contexts.WeldCreationalContext;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.EmptyInjectionPoint;
import org.jboss.weld.injection.ThreadLocalStack.ThreadLocalStackReference;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.ContextLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.serialization.spi.ContextualStore;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * An instance locator that uses a context to lookup the instance if
 * it exists; otherwise, a new instance will be created from the
 * bean.
 *
 * @author David Allen
 */
@SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "bean field is loaded lazily")
public class ContextBeanInstance<T> extends AbstractBeanInstance implements Serializable {

    private static final long serialVersionUID = -8144230657830556503L;
    // The bean
    private transient Bean<T> bean;
    // The bean index in the manager
    private final BeanIdentifier id;
    private final String contextId;
    // The actual type of the resulting bean instance
    private final transient Class<?> instanceType;
    private final transient BeanManagerImpl manager;
    private final transient CurrentInjectionPoint currentInjectionPoint;

    private static final ThreadLocal<WeldCreationalContext<?>> currentCreationalContext = new ThreadLocal<WeldCreationalContext<?>>();

    /**
     * Creates a new locator for instances of the given bean.
     *
     * @param bean The contextual bean
     * @param id The unique identifier of this bean
     */
    public ContextBeanInstance(Bean<T> bean, BeanIdentifier id, String contextId) {
        this.bean = bean;
        this.id = id;
        this.contextId = contextId;
        this.instanceType = computeInstanceType(bean);
        BeanLogger.LOG.createdContextInstance(bean, id);
        this.manager = Container.instance(contextId).deploymentManager();
        this.currentInjectionPoint = manager.getServices().get(CurrentInjectionPoint.class);
    }

    public T getInstance() {
        if (!Container.isSet(contextId)) {
            throw ContextLogger.LOG.contextualReferenceNotValidAfterShutdown(bean, contextId);
        }
        T existingInstance = ContextualInstance.getIfExists(bean, manager);
        if (existingInstance != null) {
            return existingInstance;
        }
        WeldCreationalContext<T> creationalContext;
        WeldCreationalContext<?> previousCreationalContext = currentCreationalContext.get();
        if (previousCreationalContext == null) {
            creationalContext = new CreationalContextImpl<T>(bean);
        } else {
            creationalContext = previousCreationalContext.getCreationalContext(bean);
        }
        currentCreationalContext.set(creationalContext);
        // Ensure that there is no injection point associated
        final ThreadLocalStackReference<InjectionPoint> stack = currentInjectionPoint.push(EmptyInjectionPoint.INSTANCE);
        try {
            return ContextualInstance.get(bean, manager, creationalContext);
        } finally {
            stack.pop();
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

    private Object readResolve() throws ObjectStreamException {
        Bean<T> bean = Container.instance(contextId).services().get(ContextualStore.class).<Bean<T>, T> getContextual(id);
        return new ContextBeanInstance<T>(bean, id, contextId);
    }

}
