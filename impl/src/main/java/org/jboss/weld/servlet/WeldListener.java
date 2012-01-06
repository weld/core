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
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.cache.RequestScopedBeanCache;
import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.servlet.api.helpers.AbstractServletListener;
import org.slf4j.cal10n.LocLogger;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;

import static org.jboss.weld.logging.Category.SERVLET;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ConversationMessage.CLEANING_UP_TRANSIENT_CONVERSATION;
import static org.jboss.weld.logging.messages.JsfMessage.CLEANING_UP_CONVERSATION;
import static org.jboss.weld.logging.messages.JsfMessage.FOUND_CONVERSATION_FROM_REQUEST;
import static org.jboss.weld.logging.messages.JsfMessage.RESUMING_CONVERSATION;
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
 * @author Dan Allen
 * @author Ales Justin
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author Jozef Hartinger
 */
public class WeldListener extends AbstractServletListener {

    private static final String NO_CID = "nocid";
    private static final String CONVERSATION_PROPAGATION = "conversationPropagation";
    private static final String CONVERSATION_PROPAGATION_NONE = "none";
    private static final String CONTEXT_ACTIVATED_IN_REQUEST = WeldListener.class.getName() + "CONTEXT_ACTIVATED_IN_REQUEST";

    private static final LocLogger log = loggerFactory().getLogger(SERVLET);

    private transient HttpSessionContext sessionContextCache;
    private transient HttpRequestContext requestContextCache;
    private transient HttpConversationContext conversationContextCache;

    @Inject
    private BeanManager beanManager;

    private HttpSessionContext sessionContext() {
        if (sessionContextCache == null) {
            this.sessionContextCache = Container.instance().deploymentManager().instance().select(HttpSessionContext.class).get();
        }
        return sessionContextCache;
    }

    private HttpRequestContext requestContext() {
        if (requestContextCache == null) {
            this.requestContextCache = Container.instance().deploymentManager().instance().select(HttpRequestContext.class).get();
        }
        return requestContextCache;
    }

    private HttpConversationContext conversationContext() {
        if (conversationContextCache == null) {
            this.conversationContextCache = Container.instance().deploymentManager().instance().select(HttpConversationContext.class).get();
        }
        return conversationContextCache;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setAttribute(BeanManager.class.getName(), beanManager);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        // JBoss AS will still start the deployment even if WB fails
        if (Container.available()) {
            // Mark the session context and conversation contexts to destroy
            // instances when appropriate
            sessionContext().destroy(event.getSession());
            RequestScopedBeanCache.endRequest();
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        log.trace(REQUEST_DESTROYED, event.getServletRequest());
        // JBoss AS will still start the deployment even if WB fails
        if (Container.available()) {
            if (event.getServletRequest() instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) event.getServletRequest();

                try {
                    deactivateConversations(request);
                    requestContext().invalidate();
                    requestContext().deactivate();
                    sessionContext().deactivate();
                } finally {
                    requestContext().dissociate(request);
                    sessionContext().dissociate(request);
                    conversationContext().dissociate(request);
                }
            } else {
                throw new IllegalStateException(ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED);
            }
        }
    }

    /**
     * Execute after the Render Response phase.
     */
    private void deactivateConversations(HttpServletRequest request) {
        ConversationContext conversationContext = instance().select(HttpConversationContext.class).get();
        if (log.isTraceEnabled()) {
            if (conversationContext.getCurrentConversation().isTransient()) {
                log.trace(CLEANING_UP_TRANSIENT_CONVERSATION);
            } else {
                log.trace(CLEANING_UP_CONVERSATION, conversationContext.getCurrentConversation().getId());
            }
        }
        conversationContext.invalidate();
        if (conversationContext.isActive()) {
            // Only deactivate the context if one is already active, otherwise we get Exceptions
            conversationContext.deactivate();
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        log.trace(REQUEST_INITIALIZED, event.getServletRequest());
        // JBoss AS will still start the deployment even if Weld fails to start
        if (Container.available()) {
            if (event.getServletRequest() instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) event.getServletRequest();

                requestContext().associate(request);
                sessionContext().associate(request);
                conversationContext().associate(request);

                requestContext().activate();
                sessionContext().activate();

                /*
                 * This is just wrong.
                 * If an exception occurs during conversation activation (e.g. NonexistentConversationException), the
                 * requestDestroyed callback is never invoked (per spec) to cleanup the threadlocals.
                 *
                 * Also, there is no way for an application to catch the application and handle it gracefully. This needs to be
                 * fixed/clarified in the CDI spec (CDI-206).
                 */
                try {
                    activateConversations(request);
                } catch (RuntimeException e) {
                    requestDestroyed(event);
                    throw e;
                }
            } else {
                throw new IllegalStateException(ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED);
            }
        }
    }

    // Conversation handling

    private void activateConversations(HttpServletRequest request) {
        HttpConversationContext conversationContext = instance().select(HttpConversationContext.class).get();
        String cid = getConversationId(request, conversationContext);
        log.debug(RESUMING_CONVERSATION, cid);

        /*
         * Don't try to reactivate the ConversationContext if we have already activated it for this request WELD-877
         */
        if (!isContextActivatedInRequest(request)) {
            setContextActivatedInRequest(request);
            conversationContext.activate(cid);
        } else {
            /*
             * We may have previously been associated with a ConversationContext, but the reference to that context may have
             * been lost during a Servlet forward WELD-877
             */
            conversationContext.dissociate(request);
            conversationContext.associate(request);
            conversationContext.activate(cid);
        }
    }

    private void setContextActivatedInRequest(HttpServletRequest request) {
        request.setAttribute(CONTEXT_ACTIVATED_IN_REQUEST, true);
    }

    private boolean isContextActivatedInRequest(HttpServletRequest request) {
        Object result = request.getAttribute(CONTEXT_ACTIVATED_IN_REQUEST);
        if (result == null) {
            return false;
        }
        return (Boolean) result;
    }

    private static Instance<Context> instance() {
        return Container.instance().deploymentManager().instance().select(Context.class);
    }

    /**
     * Gets the propagated conversation id parameter from the request
     *
     * @return The conversation id (or null if not found)
     */
    public static String getConversationId(HttpServletRequest request, ConversationContext conversationContext) {
        if (request.getParameter(NO_CID) != null) {
            return null; // ignore cid; WELD-919
        }

        if (CONVERSATION_PROPAGATION_NONE.equals(request.getParameter(CONVERSATION_PROPAGATION))) {
            return null; // conversationPropagation=none (CDI-135)
        }

        String cidName = conversationContext.getParameterName();
        String cid = request.getParameter(cidName);
        log.trace(FOUND_CONVERSATION_FROM_REQUEST, cid);
        return cid;
    }

}
