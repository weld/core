/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.builtin;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;
import org.jboss.weld.event.FastEvent;
import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 *
 * @author Martin Kouba
 */
public class RequestContextControllerBean extends AbstractStaticallyDecorableBuiltInBean<RequestContextController> {

    public RequestContextControllerBean(BeanManagerImpl beanManager) {
        super(beanManager, RequestContextController.class);
    }

    @Override
    protected RequestContextController newInstance(InjectionPoint ip,
            CreationalContext<RequestContextController> creationalContext) {
        return new InjectableRequestContextController(beanManager, getUnboundRequestContext());
    }

    private RequestContext getUnboundRequestContext() {
        final Bean<?> bean = beanManager.resolve(beanManager.getBeans(RequestContext.class, UnboundLiteral.INSTANCE));
        final CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
        return (RequestContext) beanManager.getReference(bean, RequestContext.class, ctx);
    }

    private static class InjectableRequestContextController implements RequestContextController {

        private final BeanManagerImpl beanManager;

        private final RequestContext requestContext;

        private final AtomicBoolean isActivator;

        private final FastEvent<Object> requestInitializedEvent;
        private final FastEvent<Object> requestBeforeDestroyedEvent;
        private final FastEvent<Object> requestDestroyedEvent;

        InjectableRequestContextController(BeanManagerImpl beanManager, RequestContext requestContext) {
            this.beanManager = beanManager;
            this.requestContext = requestContext;
            this.isActivator = new AtomicBoolean(false);
            this.requestInitializedEvent = FastEvent.of(Object.class, beanManager, Initialized.Literal.REQUEST);
            this.requestBeforeDestroyedEvent = FastEvent.of(Object.class, beanManager, BeforeDestroyed.Literal.REQUEST);
            this.requestDestroyedEvent = FastEvent.of(Object.class, beanManager, Destroyed.Literal.REQUEST);
        }

        @Override
        public boolean activate() {
            if (isRequestContextActive()) {
                return false;
            }
            requestContext.activate();
            requestInitializedEvent.fire(toString());
            isActivator.set(true);
            return true;
        }

        @Override
        public void deactivate() throws ContextNotActiveException {
            if (!isRequestContextActive()) {
                throw BeanManagerLogger.LOG.contextNotActive(RequestScoped.class);
            }
            if (isActivator.compareAndSet(true, false)) {
                try {
                    requestBeforeDestroyedEvent.fire(toString());
                    requestContext.invalidate();
                    requestContext.deactivate();
                } finally {
                    requestDestroyedEvent.fire(toString());
                }
            }
        }

        private boolean isRequestContextActive() {
            return beanManager.isContextActive(RequestScoped.class);
        }

    }

}
