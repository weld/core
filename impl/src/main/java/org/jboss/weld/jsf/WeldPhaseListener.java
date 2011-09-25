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
package org.jboss.weld.jsf;

import static javax.faces.event.PhaseId.ANY_PHASE;
import static javax.faces.event.PhaseId.RENDER_RESPONSE;
import static javax.faces.event.PhaseId.RESTORE_VIEW;
import static org.jboss.weld.logging.Category.JSF;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ConversationMessage.CLEANING_UP_TRANSIENT_CONVERSATION;
import static org.jboss.weld.logging.messages.JsfMessage.CLEANING_UP_CONVERSATION;
import static org.jboss.weld.logging.messages.JsfMessage.FOUND_CONVERSATION_FROM_REQUEST;
import static org.jboss.weld.logging.messages.JsfMessage.RESUMING_CONVERSATION;

import java.util.Map;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Instance;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.jboss.weld.Container;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.http.HttpConversationContext;
import org.slf4j.cal10n.LocLogger;

/**
 * <p>
 * A JSF phase listener that initializes aspects of Weld in a more fine-grained, integrated manner than what is possible with a
 * servlet filter. This phase listener works in conjunction with other hooks and callbacks registered with the JSF runtime to
 * help manage the Weld lifecycle.
 * </p>
 * <p/>
 * <p>
 * The phase listener restores the long-running conversation if the conversation id token is detected in the request, activates
 * the conversation context in either case (long-running or transient), and finally passivates the conversation after the
 * response has been committed.
 * </p>
 * <p/>
 * <p>
 * Execute before every phase in the JSF life cycle. The order this phase listener executes in relation to other phase listeners
 * is determined by the ordering of the faces-config.xml descriptors. This phase listener should take precedence over
 * extensions.
 * </p>
 *
 * @author Nicklas Karlsson
 * @author Dan Allen
 * @author Ales Justin
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WeldPhaseListener implements PhaseListener {
    private static final long serialVersionUID = 1L;

    private static final LocLogger log = loggerFactory().getLogger(JSF);

    private String contextId;

    public static final String NO_CID = "nocid";

    private static final String CONTEXT_ACTIVATED_IN_REQUEST = WeldPhaseListener.class.getName()
            + "CONTEXT_ACTIVATED_IN_REQUEST";

    public void beforePhase(PhaseEvent phaseEvent) {
        if (phaseEvent.getPhaseId().equals(RESTORE_VIEW)) {
            activateConversations(phaseEvent.getFacesContext());
        }
    }

    public void afterPhase(PhaseEvent phaseEvent) {
        if (phaseEvent.getPhaseId().equals(RENDER_RESPONSE)) {
            deactivateConversations(phaseEvent.getFacesContext(), RENDER_RESPONSE);
        } else if (phaseEvent.getFacesContext().getResponseComplete()) {
            deactivateConversations(phaseEvent.getFacesContext(), phaseEvent.getPhaseId());
        }
    }

    private void activateConversations(FacesContext facesContext) {
        HttpConversationContext conversationContext = instance(contextId).select(HttpConversationContext.class).get();
        String cid = getConversationId(facesContext, conversationContext);
        log.debug(RESUMING_CONVERSATION, cid);

        /*
         * Don't try to reactivate the ConversationContext if we have already activated it for this request
         * WELD-877
         */
        if (!isContextActivatedInRequest(facesContext)) {
            setContextActivatedInRequest(facesContext);
            conversationContext.activate(cid);
        } else {
            /*
             *  We may have previously been associated with a ConversationContext,
             *  but the reference to that context may have been lost during a Servlet forward
             *  WELD-877
             */
            HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
            conversationContext.dissociate(request);
            conversationContext.associate(request);
            conversationContext.activate(cid);
        }
    }

    private void setContextActivatedInRequest(FacesContext facesContext) {
        facesContext.getExternalContext().getRequestMap().put(CONTEXT_ACTIVATED_IN_REQUEST, true);
    }

    private boolean isContextActivatedInRequest(FacesContext facesContext) {
        Object result = facesContext.getExternalContext().getRequestMap().get(CONTEXT_ACTIVATED_IN_REQUEST);
        if (result == null) {
            return false;
        }
        return (Boolean) result;
    }

    /**
     * Execute after the Render Response phase.
     */
    private void deactivateConversations(FacesContext facesContext, PhaseId phaseId) {
        if (contextId == null) {
            if (facesContext.getAttributes().containsKey(Container.CONTEXT_ID_KEY)) {
                contextId = (String) facesContext.getAttributes().get(Container.CONTEXT_ID_KEY);
            }
        }
        ConversationContext conversationContext = instance(contextId).select(HttpConversationContext.class).get();
        if (log.isTraceEnabled()) {
            if (conversationContext.getCurrentConversation().isTransient()) {
                log.trace(CLEANING_UP_TRANSIENT_CONVERSATION, phaseId);
            } else {
                log.trace(CLEANING_UP_CONVERSATION, conversationContext.getCurrentConversation().getId(), phaseId);
            }
        }
        conversationContext.invalidate();
        if(conversationContext.isActive()) {
            // Only deactivate the context if one is already active, otherwise we get Exceptions
            conversationContext.deactivate();
        }
    }

    /**
     * The phase id for which this phase listener is active. This phase listener observes all JSF life-cycle phases.
     */
    public PhaseId getPhaseId() {
        return ANY_PHASE;
    }

    private Instance<Context> instance(String id) {
        return Container.instance(id).deploymentManager().instance().select(Context.class);
    }

    /**
     * Gets the propagated conversation id parameter from the request
     *
     * @return The conversation id (or null if not found)
     */
    public String getConversationId(FacesContext facesContext, ConversationContext conversationContext) {
        Map<String, String> map = facesContext.getExternalContext().getRequestParameterMap();
        if (map.containsKey(NO_CID))
            return null; // ignore cid; WELD-919

        String cidName = conversationContext.getParameterName();
        String cid = map.get(cidName);
        log.trace(FOUND_CONVERSATION_FROM_REQUEST, cid);
        return cid;
    }

}
