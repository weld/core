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

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.ejb.EjbRequestContext;

import javax.interceptor.InvocationContext;
import java.io.Serializable;

/**
 * Interceptor for ensuring the request context is active during requests to EJBs.
 * <p/>
 * Normally, a servlet will start the request context, however in non-servlet
 * requests (e.g. MDB, async, timeout) the contexts may need starting.
 * <p/>
 * The Application context is active for duration of the deployment
 *
 * @author Pete Muir
 */
public class SessionBeanInterceptor implements Serializable {
    private static final long serialVersionUID = 7327757031821596782L;

    private String contextId;

    public Object aroundInvoke(InvocationContext invocation) throws Exception {

        if (isRequestContextActive()) {
            return invocation.proceed();
        } else {
            if (contextId == null) {
                if (invocation.getContextData().containsKey(Container.CONTEXT_ID_KEY)) {
                    contextId = (String) invocation.getContextData().get(Container.CONTEXT_ID_KEY);
                } else {
                    contextId = RegistrySingletonProvider.STATIC_INSTANCE;
                }
            }
            EjbRequestContext requestContext = Container.instance(contextId).deploymentManager().instance().select(EjbRequestContext.class).get();
            try {
                requestContext.associate(invocation);
                requestContext.activate();
                try {
                    return invocation.proceed();
                } finally {
                    requestContext.invalidate();
                    requestContext.deactivate();

                }
            } finally {
                requestContext.dissociate(invocation);
            }
        }
    }

    private boolean isRequestContextActive() {
        for (RequestContext requestContext : Container.instance(contextId).deploymentManager().instance().select(RequestContext.class)) {
            if (requestContext.isActive()) {
                return true;
            }
        }
        return false;
    }

}

