/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.logging.Category.SERVLET;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ConversationMessage.CLEANING_UP_TRANSIENT_CONVERSATION;
import static org.jboss.weld.logging.messages.JsfMessage.CLEANING_UP_CONVERSATION;
import static org.jboss.weld.logging.messages.JsfMessage.FOUND_CONVERSATION_FROM_REQUEST;
import static org.jboss.weld.logging.messages.JsfMessage.RESUMING_CONVERSATION;

import javax.enterprise.inject.Instance;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.literal.DestroyedLiteral;
import org.jboss.weld.literal.InitializedLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.slf4j.cal10n.LocLogger;

/**
 * This component takes care of activation/deactivation of the conversation context for a servlet request.
 *
 * @see ConversationFilter
 * @see WeldListener
 *
 * @author Jozef Hartinger
 *
 */
public class ConversationContextActivator {

    private static final String NO_CID = "nocid";
    private static final String CONVERSATION_PROPAGATION = "conversationPropagation";
    private static final String CONVERSATION_PROPAGATION_NONE = "none";
    private static final String CONTEXT_ACTIVATED_IN_REQUEST = WeldListener.class.getName() + "CONTEXT_ACTIVATED_IN_REQUEST";

    private static final LocLogger log = loggerFactory().getLogger(SERVLET);

    private final BeanManagerImpl beanManager;
    private final Instance<HttpConversationContext> httpConversationContext;
    private final ServletContext ctx;

    protected ConversationContextActivator(BeanManagerImpl beanManager, ServletContext ctx) {
        this.beanManager = beanManager;
        this.httpConversationContext = beanManager.instance().select(HttpConversationContext.class);
        this.ctx = ctx;
    }

    public void startConversationContext(HttpServletRequest request) {
        associateConversationContext(request);
        activateConversationContext(request);
    }

    public void stopConversationContext(HttpServletRequest request) {
        deactivateConversationContext(request);

    }

    // Conversation handling

    protected void activateConversationContext(HttpServletRequest request) {
        HttpConversationContext conversationContext = httpConversationContext.get();
        String cid = getConversationId(request, conversationContext);
        log.debug(RESUMING_CONVERSATION, cid);

        /*
         * Don't try to reactivate the ConversationContext if we have already activated it for this request WELD-877
         */
        if (!isContextActivatedInRequest(request)) {
            setContextActivatedInRequest(request);
            conversationContext.activate(cid);
            if (cid == null) { // transient conversation
                beanManager.getAccessibleLenientObserverNotifier().fireEvent(buildServletRequestEvent(request), InitializedLiteral.CONVERSATION);
            }
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

    protected void associateConversationContext(HttpServletRequest request) {
        httpConversationContext.get().associate(request);
    }

    /**
     * Gets the propagated conversation id parameter from the request
     *
     * @return The conversation id (or null if not found)
     */
    private static String getConversationId(HttpServletRequest request, ConversationContext conversationContext) {
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

    // TODO: we should NOT be rebuilding it
    private ServletRequestEvent buildServletRequestEvent(ServletRequest request) {
        return new ServletRequestEvent(ctx, request);
    }

    protected void deactivateConversationContext(HttpServletRequest request) {
        ConversationContext conversationContext = httpConversationContext.get();
        boolean isTransient = conversationContext.getCurrentConversation().isTransient();
        if (log.isTraceEnabled()) {
            if (isTransient) {
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
        if (isTransient) {
            beanManager.getAccessibleLenientObserverNotifier().fireEvent(buildServletRequestEvent(request), DestroyedLiteral.CONVERSATION);
        }
    }

    protected void disassociateConversationContext(HttpServletRequest request) {
        httpConversationContext.get().dissociate(request);
    }
}
