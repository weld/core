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

import static org.jboss.weld.jsf.JsfHelper.getConversationId;
import static org.jboss.weld.jsf.JsfHelper.getHttpSession;
import static org.jboss.weld.jsf.JsfHelper.getServletContext;
import static org.jboss.weld.logging.Category.JSF;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.JsfMessage.CLEANING_UP_CONVERSATION;
import static org.jboss.weld.logging.messages.JsfMessage.INITIATING_CONVERSATION;
import static org.jboss.weld.logging.messages.JsfMessage.SKIPPING_CLEANING_UP_CONVERSATION;
import static org.jboss.weld.servlet.BeanProvider.conversation;
import static org.jboss.weld.servlet.BeanProvider.conversationManager;
import static org.jboss.weld.servlet.BeanProvider.httpSessionManager;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.jboss.weld.Container;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.SessionContext;
import org.jboss.weld.conversation.AbstractConversationManager;
import org.slf4j.cal10n.LocLogger;

/**
 * <p>
 * A JSF phase listener that initializes aspects of Weld in a more
 * fine-grained, integrated manner than what is possible with a servlet filter.
 * This phase listener works in conjunction with other hooks and callbacks
 * registered with the JSF runtime to help manage the Weld lifecycle.
 * </p>
 * 
 * <p>
 * It's expected that over time, this phase listener may take on more work, but
 * for now the work is focused soley on conversation management. The phase
 * listener restores the long-running conversation if the conversation id token
 * is detected in the request, activates the conversation context in either case
 * (long-running or transient), and finally passivates the conversation after
 * the response has been committed.
 * </p>
 * 
 * @author Nicklas Karlsson
 * @author Dan Allen
 */
public class WeldPhaseListener implements PhaseListener
{
   private static final LocLogger log = loggerFactory().getLogger(JSF);

   /**
    * Execute before every phase in the JSF life cycle. The order this
    * phase listener executes in relation to other phase listeners is
    * determined by the ordering of the faces-config.xml descriptors.
    * This phase listener should take precedence over extensions.
    * 
    * @param phaseEvent The phase event
    */
   public void beforePhase(PhaseEvent phaseEvent)
   {
      if (phaseEvent.getPhaseId().equals(PhaseId.RESTORE_VIEW))
      {
         beforeRestoreView(phaseEvent.getFacesContext());
      }
   }

   /**
    * Execute after every phase in the JSF life cycle. The order this
    * phase listener executes in relation to other phase listeners is
    * determined by the ordering of the faces-config.xml descriptors.
    * This phase listener should take precedence over extensions.
    * 
    * @param phaseEvent The phase event
    */
   public void afterPhase(PhaseEvent phaseEvent)
   {
      if (phaseEvent.getPhaseId().equals(PhaseId.RENDER_RESPONSE))
      {
         afterRenderResponse(phaseEvent.getFacesContext());
      }
      // be careful with this else as it assumes only one if condition right now
      else if (phaseEvent.getFacesContext().getResponseComplete())
      {
         afterResponseComplete(phaseEvent.getFacesContext(), phaseEvent.getPhaseId());
      }
   }

   /**
    * Execute before the Restore View phase.
    */
   private void beforeRestoreView(FacesContext facesContext)
   {
      log.trace(INITIATING_CONVERSATION, "Restore View");
      initiateSessionAndConversation(facesContext);
   }
   
   /**
    * Execute after the Render Response phase.
    */
   private void afterRenderResponse(FacesContext facesContext)
   {
      SessionContext sessionContext = Container.instance().services().get(ContextLifecycle.class).getSessionContext();
      ConversationContext conversationContext = Container.instance().services().get(ContextLifecycle.class).getConversationContext();
      if (sessionContext.isActive())
      {
         log.trace(CLEANING_UP_CONVERSATION, "Render Response", "response complete");
         conversationManager(getServletContext(facesContext)).cleanupConversation();
         conversationContext.setActive(false);
      }
      else
      {
         log.trace(SKIPPING_CLEANING_UP_CONVERSATION, "Render Response", "session has been terminated");
      }
   }

   /**
    * Execute after any phase that marks the response as complete.
    */
   private void afterResponseComplete(FacesContext facesContext, PhaseId phaseId)
   {
      SessionContext sessionContext = Container.instance().services().get(ContextLifecycle.class).getSessionContext();
      if (sessionContext.isActive())
      {
         log.trace(CLEANING_UP_CONVERSATION, phaseId, "the response has been marked complete");
         conversationManager(getServletContext(facesContext)).cleanupConversation();
      }
      else
      {
         log.trace(SKIPPING_CLEANING_UP_CONVERSATION, phaseId, "session has been terminated");
      }
   }

   /**
    * Retrieve the HTTP session from the FacesContext and assign it to the Web
    * Beans HttpSessionManager. Restore the long-running conversation if the
    * conversation id token is present in the request and, in either case,
    * activate the conversation context (long-running or transient).
    */
   private void initiateSessionAndConversation(FacesContext facesContext)
   {
      ServletContext servletContext = getServletContext(facesContext);
      AbstractConversationManager conversationManager = (AbstractConversationManager) conversationManager(servletContext);
      HttpSession session = getHttpSession(facesContext);
      httpSessionManager(servletContext).setSession(session);
      try
      {
         conversationManager.beginOrRestoreConversation(getConversationId(facesContext));
      }
      finally
      {
         String cid = conversation(servletContext).getUnderlyingId();
         
         ConversationContext conversationContext = Container.instance().services().get(ContextLifecycle.class).getConversationContext();
         conversationContext.setBeanStore(conversationManager.getBeanStore(cid));
         conversationContext.setActive(true);
      }
   }

   /**
    * The phase id for which this phase listener is active. This phase listener
    * observes all JSF life-cycle phases.
    */
   public PhaseId getPhaseId()
   {
      return PhaseId.ANY_PHASE;
   }

}
