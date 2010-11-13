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
import static org.jboss.weld.logging.messages.ConversationMessage.NO_CONVERSATION_TO_RESTORE;
import static org.jboss.weld.logging.messages.JsfMessage.CLEANING_UP_CONVERSATION;
import static org.jboss.weld.logging.messages.JsfMessage.FOUND_CONVERSATION_FROM_REQUEST;
import static org.jboss.weld.logging.messages.JsfMessage.RESUMING_CONVERSATION;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Instance;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.jboss.weld.Container;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.NonexistentConversationException;
import org.jboss.weld.context.http.HttpConversationContext;
import org.slf4j.cal10n.LocLogger;

/**
 * <p>
 * A JSF phase listener that initializes aspects of Weld in a more fine-grained,
 * integrated manner than what is possible with a servlet filter. This phase
 * listener works in conjunction with other hooks and callbacks registered with
 * the JSF runtime to help manage the Weld lifecycle.
 * </p>
 * 
 * <p>
 * The phase listener restores the long-running conversation if the conversation
 * id token is detected in the request, activates the conversation context in
 * either case (long-running or transient), and finally passivates the
 * conversation after the response has been committed.
 * </p>
 * 
 * <p>
 * Execute before every phase in the JSF life cycle. The order this phase
 * listener executes in relation to other phase listeners is determined by the
 * ordering of the faces-config.xml descriptors. This phase listener should take
 * precedence over extensions.
 * </p>
 * 
 * @author Nicklas Karlsson
 * @author Dan Allen
 */
public class WeldPhaseListener implements PhaseListener
{
   private static final long serialVersionUID = 1L;

   private static final LocLogger log = loggerFactory().getLogger(JSF);

   public void beforePhase(PhaseEvent phaseEvent)
   {
      if (phaseEvent.getPhaseId().equals(RESTORE_VIEW))
      {
         activateConversations(phaseEvent.getFacesContext());
      }
   }

   public void afterPhase(PhaseEvent phaseEvent)
   {
      if (phaseEvent.getPhaseId().equals(RENDER_RESPONSE))
      {
         deactivateConversations(phaseEvent.getFacesContext(), RENDER_RESPONSE);
      }
      else if (phaseEvent.getFacesContext().getResponseComplete())
      {
         deactivateConversations(phaseEvent.getFacesContext(), phaseEvent.getPhaseId());
      }
   }

   private void activateConversations(FacesContext facesContext)
   {
      ConversationContext conversationContext = instance().select(HttpConversationContext.class).get();
      String cid = getConversationId(facesContext, conversationContext);
      log.debug(RESUMING_CONVERSATION, cid);
      if (cid != null && conversationContext.getConversation(cid) == null)
      {
         // Make sure that the conversation already exists
         throw new NonexistentConversationException(NO_CONVERSATION_TO_RESTORE, cid);
      }
      conversationContext.activate(cid);
   }

   /**
    * Execute after the Render Response phase.
    */
   private void deactivateConversations(FacesContext facesContext, PhaseId phaseId)
   {
      ConversationContext conversationContext = instance().select(HttpConversationContext.class).get();
      if (log.isTraceEnabled())
      {
         if (conversationContext.getCurrentConversation().isTransient())
         {
            log.trace(CLEANING_UP_TRANSIENT_CONVERSATION, phaseId);
         }
         else
         {
            log.trace(CLEANING_UP_CONVERSATION, conversationContext.getCurrentConversation().getId(), phaseId);
         }
      }
      conversationContext.invalidate();
      conversationContext.deactivate();
   }

   /**
    * The phase id for which this phase listener is active. This phase listener
    * observes all JSF life-cycle phases.
    */
   public PhaseId getPhaseId()
   {
      return ANY_PHASE;
   }
   
   private static Instance<Context> instance()
   {
      return Container.instance().deploymentManager().instance().select(Context.class);
   }
   
   /**
    * Gets the propagated conversation id parameter from the request
    * 
    * @return The conversation id (or null if not found)
    */
   public static String getConversationId(FacesContext facesContext, ConversationContext conversationContext)
   {
      String cidName = conversationContext.getParameterName();
      String cid = facesContext.getExternalContext().getRequestParameterMap().get(cidName);
      log.trace(FOUND_CONVERSATION_FROM_REQUEST, cid);
      return cid;
   }

}
