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
package org.jboss.weld.contexts.activator;

import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.event.FastEvent;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Base class for Request Context activation via interceptor.
 * Can be activated using binding from Weld API (since 2.4) and also via CDI API binding (since 2.0).
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public abstract class AbstractActivateRequestContextInterceptor {

    protected final BeanManagerImpl beanManager;
    protected final RequestContext requestContext;
    protected final FastEvent<Object> fastEventInit;
    protected final FastEvent<Object> fastEventBeforeDestroyed;
    protected final FastEvent<Object> fastEventDestroyed;

    public AbstractActivateRequestContextInterceptor(RequestContext requestContext, BeanManagerImpl beanManager) {
        this.beanManager = beanManager;
        this.requestContext = requestContext;
        fastEventInit = FastEvent.of(Object.class, beanManager, Initialized.Literal.REQUEST);
        fastEventBeforeDestroyed = FastEvent.of(Object.class, beanManager, BeforeDestroyed.Literal.REQUEST);
        fastEventDestroyed = FastEvent.of(Object.class, beanManager, Destroyed.Literal.REQUEST);
    }

    @AroundInvoke
    Object invoke(InvocationContext ctx) throws Exception {
        if (isRequestContextActive()) {
            return ctx.proceed();
        } else {
            Object dummyPayload = new Object();
            try {
                requestContext.activate();
                fastEventInit.fire(dummyPayload);
                return ctx.proceed();
            } finally {
                requestContext.invalidate();
                fastEventBeforeDestroyed.fire(dummyPayload);
                requestContext.deactivate();
                fastEventDestroyed.fire(dummyPayload);
            }
        }
    }

    boolean isRequestContextActive() {
        return beanManager.isContextActive(RequestScoped.class);
    }
}
