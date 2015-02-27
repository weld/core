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
package org.jboss.weld.ejb;

import java.io.Serializable;

import javax.interceptor.InvocationContext;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.ejb.EjbRequestContext;
import org.jboss.weld.event.ContextEvent;
import org.jboss.weld.event.FastEvent;
import org.jboss.weld.literal.DestroyedLiteral;
import org.jboss.weld.literal.InitializedLiteral;
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

    private static final Object INITIALIZED_EVENT = new ContextEvent("Request context initialized for EJB invocation");
    private static final Object DESTROYED_EVENT = new ContextEvent("Request context destroyed after EJB invocation");

    private final LazyValueHolder<FastEvent<Object>> requestInitializedEvent = new LazyValueHolder.Serializable<FastEvent<Object>>() {
        private static final long serialVersionUID = 1L;
        @Override
        protected FastEvent<Object> computeValue() {
            return FastEvent.of(Object.class, getBeanManager(), getBeanManager().getGlobalLenientObserverNotifier(), InitializedLiteral.REQUEST);
        }
    };
    private final LazyValueHolder<FastEvent<Object>> requestDestroyedEvent = new LazyValueHolder.Serializable<FastEvent<Object>>() {
        private static final long serialVersionUID = 1L;
        @Override
        protected FastEvent<Object> computeValue() {
            return FastEvent.of(Object.class, getBeanManager(), getBeanManager().getGlobalLenientObserverNotifier(), DestroyedLiteral.REQUEST);
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
                    requestInitializedEvent.get().fire(INITIALIZED_EVENT);
                    return invocation.proceed();
                } finally {
                    requestContext.invalidate();
                    requestContext.deactivate();
                }
            } finally {
                requestContext.dissociate(invocation);
                // An event with qualifier @Destroyed(RequestScoped.class) when the request context is destroyed
                requestDestroyedEvent.get().fire(DESTROYED_EVENT);
            }
        }
    }

    protected boolean isRequestContextActive() {
        for (RequestContext requestContext : getBeanManager().instance().select(RequestContext.class)) {
            if (requestContext.isActive()) {
                return true;
            }
        }
        return false;
    }

    protected EjbRequestContext getEjbRequestContext() {
        return getBeanManager().instance().select(EjbRequestContext.class).get();
    }

    protected abstract BeanManagerImpl getBeanManager();
}


