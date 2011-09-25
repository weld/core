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
package org.jboss.weld.context;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.context.beanstore.BeanStore;
import org.jboss.weld.context.beanstore.LockedBean;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.slf4j.cal10n.LocLogger;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.jboss.weld.logging.Category.CONTEXT;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ContextMessage.CONTEXTUAL_INSTANCE_REMOVED;
import static org.jboss.weld.logging.messages.ContextMessage.CONTEXTUAL_IS_NULL;
import static org.jboss.weld.logging.messages.ContextMessage.CONTEXT_CLEARED;
import static org.jboss.weld.logging.messages.ContextMessage.NO_BEAN_STORE_AVAILABLE;

/**
 * Base for the Context implementations. Delegates calls to the abstract
 * getBeanStore and getActive to allow for different implementations (storage
 * types and ThreadLocal vs. shared)
 *
 * @author Nicklas Karlsson
 * @author Pete Muir
 * @see org.jboss.weld.contexts.SharedContext
 * @see org.jboss.weld.context.BasicContext
 */
public abstract class AbstractContext implements Context {
    private static final LocLogger log = loggerFactory().getLogger(CONTEXT);

    private final boolean multithreaded;

    private final ServiceRegistry serviceRegistry;

    private final String contextId;

    /**
     * Constructor
     *
     */
    public AbstractContext(String contextId, boolean multithreaded) {
        this.multithreaded = multithreaded;
        this.contextId = contextId;
        this.serviceRegistry = Container.instance(contextId).services();
    }

    public String getContextId() {
        return contextId;
    }

    /**
     * Get the bean if it exists in the contexts.
     *
     * @return An instance of the bean
     * @throws ContextNotActiveException if the context is not active
     * @see javax.enterprise.context.spi.Context#get(BaseBean, boolean)
     */
    @SuppressWarnings(value = "UL_UNRELEASED_LOCK", justification = "False positive from FindBugs")
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        if (!isActive()) {
            throw new ContextNotActiveException();
        }
        BeanStore beanStore = getBeanStore();
        if (beanStore == null) {
            return null;
        }
        if (contextual == null) {
            throw new IllegalArgumentException(CONTEXTUAL_IS_NULL);
        }
        String id = getId(contextual);
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
                    beanInstance = new SerializableContextualInstanceImpl<Contextual<T>, T>(contextId, contextual, instance, creationalContext, serviceRegistry.get(ContextualStore.class));
                    getBeanStore().put(id, beanInstance);
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

    public <T> T get(Contextual<T> contextual) {
        return get(contextual, null);
    }

    private <T> void destroy(String id) {
        if (getBeanStore() == null) {
            throw new IllegalStateException(NO_BEAN_STORE_AVAILABLE, this);
        }
        ContextualInstance<T> beanInstance = getBeanStore().get(id);
        beanInstance.getContextual().destroy(beanInstance.getInstance(), beanInstance.getCreationalContext());
        log.trace(CONTEXTUAL_INSTANCE_REMOVED, id, this);
    }


    /**
     * Destroys the context
     */
    protected void destroy() {
        log.trace(CONTEXT_CLEARED, this);
        if (getBeanStore() == null) {
            throw new IllegalStateException(NO_BEAN_STORE_AVAILABLE, this);
        }
        for (String id : getBeanStore()) {
            destroy(id);
        }
        getBeanStore().clear();
    }

    /**
     * A method that returns the actual bean store implementation
     *
     * @return The bean store
     */
    protected abstract BeanStore getBeanStore();

    public void cleanup() {
        if (getBeanStore() != null) {
            getBeanStore().clear();
        }
    }

    protected static <T> Contextual<T> getContextual(String contextId, String id) {
        return Container.instance(contextId).services().get(ContextualStore.class).<Contextual<T>, T>getContextual(id);
    }

    protected String getId(Contextual<?> contextual) {
        return serviceRegistry.get(ContextualStore.class).putIfAbsent(contextual);
    }

    protected ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

}
