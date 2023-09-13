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
package org.jboss.weld.module.ejb;

import java.io.Serializable;

import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.RequestScoped;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.context.ejb.EjbRequestContext;
import org.jboss.weld.event.ContextEvent;
import org.jboss.weld.event.FastEvent;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.LazyValueHolder;

/**
 * Interceptor for ensuring the request context is active during requests to EJBs.
 * <p/>
 * Normally, a servlet will start the request context, however in non-servlet
 * requests (e.g. MDB, async, timeout) the contexts may need starting.
 * <p/>
 * The Application context is active for duration of the deployment
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public abstract class AbstractEJBRequestScopeActivationInterceptor implements Serializable {
    private static final long serialVersionUID = 7327757031821596782L;

    private final LazyValueHolder<FastEvent<Object>> requestInitializedEvent = new LazyValueHolder.Serializable<FastEvent<Object>>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected FastEvent<Object> computeValue() {
            return FastEvent.of(Object.class, getBeanManager(), getBeanManager().getGlobalLenientObserverNotifier(),
                    Initialized.Literal.REQUEST);
        }
    };
    private final LazyValueHolder<FastEvent<Object>> requestBeforeDestroyedEvent = new LazyValueHolder.Serializable<FastEvent<Object>>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected FastEvent<Object> computeValue() {
            return FastEvent.of(Object.class, getBeanManager(), getBeanManager().getGlobalLenientObserverNotifier(),
                    BeforeDestroyed.Literal.REQUEST);
        }
    };
    private final LazyValueHolder<FastEvent<Object>> requestDestroyedEvent = new LazyValueHolder.Serializable<FastEvent<Object>>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected FastEvent<Object> computeValue() {
            return FastEvent.of(Object.class, getBeanManager(), getBeanManager().getGlobalLenientObserverNotifier(),
                    Destroyed.Literal.REQUEST);
        }
    };

    public Object aroundInvoke(InvocationContext invocation) throws Exception {

        if (isRequestContextActive()) {
            return invocation.proceed();
        } else {
            EjbRequestContext requestContext = getEjbRequestContext();
            try {
                requestContext.associate(invocation);
                requestContext.activate();
                try {
                    // An event with qualifier @Initialized(RequestScoped.class) is fired when the request context is initialized
                    requestInitializedEvent.get().fire(ContextEvent.REQUEST_INITIALIZED_EJB);
                    return invocation.proceed();
                } finally {
                    requestBeforeDestroyedEvent.get().fire(ContextEvent.REQUEST_BEFORE_DESTROYED_EJB);
                    requestContext.invalidate();
                    requestContext.deactivate();
                }
            } finally {
                requestContext.dissociate(invocation);
                // An event with qualifier @Destroyed(RequestScoped.class) when the request context is destroyed
                requestDestroyedEvent.get().fire(ContextEvent.REQUEST_DESTROYED_EJB);
            }
        }
    }

    protected boolean isRequestContextActive() {
        return getBeanManager().isContextActive(RequestScoped.class);
    }

    protected EjbRequestContext getEjbRequestContext() {
        return getBeanManager().instance().select(EjbRequestContext.class).get();
    }

    protected abstract BeanManagerImpl getBeanManager();
}
