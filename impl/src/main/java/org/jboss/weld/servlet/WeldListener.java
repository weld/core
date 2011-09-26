/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
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
package org.jboss.weld.servlet;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.context.cache.RequestScopedBeanCache;
import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.servlet.api.helpers.AbstractServletListener;
import org.slf4j.cal10n.LocLogger;

import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;

import static org.jboss.weld.logging.Category.SERVLET;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ServletMessage.ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED;
import static org.jboss.weld.logging.messages.ServletMessage.REQUEST_DESTROYED;
import static org.jboss.weld.logging.messages.ServletMessage.REQUEST_INITIALIZED;

/**
 * The Weld listener
 * <p/>
 * Listens for context/session creation/destruction.
 * <p/>
 * Delegates work to the ServletLifeCycle.
 *
 * @author Nicklas Karlsson
 */
public class WeldListener extends AbstractServletListener {

    private static final LocLogger log = loggerFactory().getLogger(SERVLET);

    private transient HttpSessionContext sessionContextCache;
    private transient HttpRequestContext requestContextCache;
    private transient HttpConversationContext conversationContextCache;
    private String contextId;

    private HttpSessionContext sessionContext(String id) {
        if (sessionContextCache == null) {
            this.sessionContextCache = Container.instance(id).deploymentManager().instance().select(HttpSessionContext.class).get();
        }
        return sessionContextCache;
    }

    private HttpRequestContext requestContext(String id) {
        if (requestContextCache == null) {
            this.requestContextCache = Container.instance(id).deploymentManager().instance().select(HttpRequestContext.class).get();
        }
        return requestContextCache;
    }

    private HttpConversationContext conversationContext(String id) {
        if (conversationContextCache == null) {
            this.conversationContextCache = Container.instance(id).deploymentManager().instance().select(HttpConversationContext.class).get();
        }
        return conversationContextCache;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        if (contextId == null) {
            contextId = (String) event.getSession().getServletContext().getInitParameter(Container.CONTEXT_ID_KEY);
        }
        if (contextId == null) {
            contextId = RegistrySingletonProvider.STATIC_INSTANCE;
        }
        // JBoss AS will still start the deployment even if WB fails
        if (Container.available(contextId)) {
            // Mark the session context and conversation contexts to destroy
            // instances when appropriate
            sessionContext(contextId).destroy(event.getSession());
            RequestScopedBeanCache.endRequest();
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        if (contextId == null) {
            contextId = (String) event.getServletContext().getInitParameter(Container.CONTEXT_ID_KEY);
        }
        if (contextId == null) {
            contextId = RegistrySingletonProvider.STATIC_INSTANCE;
        }
        log.trace(REQUEST_DESTROYED, event.getServletRequest());
        // JBoss AS will still start the deployment even if WB fails
        if (Container.available(contextId)) {
            if (event.getServletRequest() instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) event.getServletRequest();

                try {
                    requestContext(contextId).invalidate();
                    requestContext(contextId).deactivate();
                    sessionContext(contextId).deactivate();
                    /*
                    * The conversation context is invalidated and deactivated in the
                    * WeldPhaseListener, however if an exception is thrown by the action
                    * method, we can't detect that in the phase listener. Make sure it
                    * happens!
                    */
                    if (conversationContext(contextId).isActive()) {
                        conversationContext(contextId).deactivate();
                    }
                } finally {
                    requestContext(contextId).dissociate(request);
                    sessionContext(contextId).dissociate(request);
                    conversationContext(contextId).dissociate(request);
                }
            } else {
                throw new IllegalStateException(ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED);
            }
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        if (contextId == null) {
            contextId = (String) event.getServletContext().getInitParameter(Container.CONTEXT_ID_KEY);
        }
        if (contextId == null) {
            contextId = RegistrySingletonProvider.STATIC_INSTANCE;
        }

        log.trace(REQUEST_INITIALIZED, event.getServletRequest());
        // JBoss AS will still start the deployment even if Weld fails to start
        if (Container.available(contextId)) {
            if (event.getServletRequest() instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) event.getServletRequest();

                requestContext(contextId).associate(request);
                sessionContext(contextId).associate(request);
                conversationContext(contextId).associate(request);
                /*
                * The conversation context is activated in the WeldPhaseListener
                */

                requestContext(contextId).activate();
                sessionContext(contextId).activate();
            } else {
                throw new IllegalStateException(ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED);
            }

        }
    }

}
