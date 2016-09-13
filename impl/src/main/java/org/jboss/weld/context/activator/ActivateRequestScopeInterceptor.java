/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.context.activator;

import javax.annotation.Priority;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.Unbound;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author Tomas Remes
 * @author Martin Kouba
 */
@Vetoed
@Interceptor
@ActivateRequestContext
@SuppressWarnings("checkstyle:magicnumber")
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 100)
public class ActivateRequestScopeInterceptor {

    private final BeanManagerImpl beanManager;

    private final RequestContext requestContext;

    @Inject
    public ActivateRequestScopeInterceptor(@Unbound RequestContext requestContext, BeanManagerImpl beanManager) {
        this.requestContext = requestContext;
        this.beanManager = beanManager;
    }

    @AroundInvoke
    Object invoke(InvocationContext ctx) throws Exception {
        if (isRequestContextActive()) {
            return ctx.proceed();
        } else {
            try {
                requestContext.activate();
                return ctx.proceed();
            } finally {
                requestContext.invalidate();
                requestContext.deactivate();
            }
        }
    }

    protected boolean isRequestContextActive() {
        return beanManager.isContextActive(RequestScoped.class);
    }
}
