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
package org.jboss.weld.contexts;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.Container;
import org.jboss.weld.bean.WrappedContextual;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.contexts.beanstore.BeanStore;
import org.jboss.weld.contexts.beanstore.LockedBean;
import org.jboss.weld.contexts.cache.RequestScopedCache;
import org.jboss.weld.logging.ContextLogger;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.Beans;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Base for the Context implementations. Delegates calls to the abstract
 * getBeanStore and getActive to allow for different implementations (storage
 * types and ThreadLocal vs. shared)
 *
 * @author Nicklas Karlsson
 * @author Pete Muir
 */
public abstract class AbstractContext implements AlterableContext {

    private final boolean multithreaded;

    private final ServiceRegistry serviceRegistry;

    /**
     * Constructor
     *
     */
    public AbstractContext(String contextId, boolean multithreaded) {
        this.multithreaded = multithreaded;
        this.serviceRegistry = Container.instance(contextId).services();
    }

    /**
     * Get the bean if it exists in the contexts.
     *
     * @return An instance of the bean
     * @throws ContextNotActiveException if the context is not active
     * @seejakarta.enterprise.context.spi.Context#get(BaseBean, boolean)
     */
    @Override
    @SuppressFBWarnings(value = "UL_UNRELEASED_LOCK", justification = "False positive from FindBugs")
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        if (!isActive()) {
            throw new ContextNotActiveException();
        }
        checkContextInitialized();
        final BeanStore beanStore = getBeanStore();
        if (beanStore == null) {
            return null;
        }
        if (contextual == null) {
            throw ContextLogger.LOG.contextualIsNull();
        }
        BeanIdentifier id = getId(contextual);
        ContextualInstance<T> beanInstance = beanStore.get(id);
        if (beanInstance != null) {
            return beanInstance.getInstance();
        } else if (creationalContext != null) {
            LockedBean lock = null;
            try {
                if (multithreaded) {
                    lock = beanStore.lock(id);
                    beanInstance = beanStore.get(id);
                    if (beanInstance != null) {
                        return beanInstance.getInstance();
                    }
                }
                T instance = contextual.create(creationalContext);
                if (instance != null) {
                    beanInstance = new SerializableContextualInstanceImpl<Contextual<T>, T>(contextual, instance,
                            creationalContext, serviceRegistry.get(ContextualStore.class));
                    beanStore.put(id, beanInstance);
                }
                return instance;
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        return get(contextual, null);
    }

    @Override
    public void destroy(Contextual<?> contextual) {
        if (!isActive()) {
            throw new ContextNotActiveException();
        }
        checkContextInitialized();
        if (contextual == null) {
            throw ContextLogger.LOG.contextualIsNull();
        }
        final BeanStore beanStore = getBeanStore();
        if (beanStore == null) {
            throw ContextLogger.LOG.noBeanStoreAvailable(this);
        }
        BeanIdentifier id = getId(contextual);
        ContextualInstance<?> beanInstance = beanStore.remove(id);
        if (beanInstance != null) {
            RequestScopedCache.invalidate();
            destroyContextualInstance(beanInstance);
        }
    }

    private <T> void destroyContextualInstance(ContextualInstance<T> instance) {
        instance.getContextual().destroy(instance.getInstance(), instance.getCreationalContext());
        ContextLogger.LOG.contextualInstanceRemoved(instance, this);
    }

    /**
     * Destroys the context
     */
    protected void destroy() {
        ContextLogger.LOG.contextCleared(this);
        final BeanStore beanStore = getBeanStore();
        if (beanStore == null) {
            throw ContextLogger.LOG.noBeanStoreAvailable(this);
        }
        for (BeanIdentifier id : beanStore) {
            destroyContextualInstance(beanStore.get(id));
        }
        beanStore.clear();
    }

    /**
     * A method that returns the actual bean store implementation
     *
     * @return The bean store
     */
    protected abstract BeanStore getBeanStore();

    public void cleanup() {
        final BeanStore beanStore = getBeanStore();
        if (beanStore != null) {
            try {
                beanStore.clear();
            } catch (Exception e) {
                ContextLogger.LOG.unableToClearBeanStore(beanStore);
                ContextLogger.LOG.catchingDebug(e);
            }
        }
    }

    protected static <T> Contextual<T> getContextual(String contextId, String id) {
        return Container.instance(contextId).services().get(ContextualStore.class).<Contextual<T>, T> getContextual(id);
    }

    protected BeanIdentifier getId(Contextual<?> contextual) {
        if (contextual instanceof WrappedContextual<?>) {
            contextual = ((WrappedContextual<?>) contextual).delegate();
        }
        return Beans.getIdentifier(contextual, serviceRegistry);
    }

    protected ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    /**
     * Allows contexts that are initialized lazily to plug in additional logic.
     */
    protected void checkContextInitialized() {
    }

    protected boolean isMultithreaded() {
        return multithreaded;
    }

}
