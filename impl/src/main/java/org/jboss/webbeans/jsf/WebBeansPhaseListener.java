/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.webbeans.jsf;

import javax.enterprise.context.Conversation;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpSession;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.context.ConversationContext;
import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.conversation.ConversationManager;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.servlet.ConversationBeanStore;
import org.jboss.webbeans.servlet.HttpSessionManager;

/**
 * <p>
 * A JSF phase listener that initializes aspects of Web Beans in a more
 * fine-grained, integrated manner than what is possible with a servlet filter.
 * This phase listener works in conjunction with other hooks and callbacks
 * registered with the JSF runtime to help manage the Web Beans lifecycle.
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
public class WebBeansPhaseListener implements PhaseListener
{
   private static LogProvider log = Logging.getLogProvider(WebBeansPhaseListener.class);

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
         beforeRestoreView();
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
         afterRenderResponse();
      }
      // be careful with this else as it assumes only one if condition right now
      else if (phaseEvent.getFacesContext().getResponseComplete())
      {
         afterResponseComplete(phaseEvent.getPhaseId());
      }
   }

   /**
    * Execute before the Restore View phase.
    */
   private void beforeRestoreView()
   {
      log.trace("Initiating the session and conversation before the Restore View phase");
      initiateSessionAndConversation();
   }
   
   /**
    * Execute after the Render Response phase.
    */
   private void afterRenderResponse()
   {
      SessionContext sessionContext = CurrentManager.rootManager().getServices().get(SessionContext.class);
      ConversationContext conversationContext = CurrentManager.rootManager().getServices().get(ConversationContext.class);
      if (sessionContext.isActive())
      {
         log.trace("Cleaning up the conversation after the Render Response phase");
         CurrentManager.rootManager().getInstanceByType(ConversationManager.class).cleanupConversation();
         conversationContext.setActive(false);
      }
      else
      {
         log.trace("Skipping conversation cleanup after the Render Response phase because session has been terminated.");
      }
   }

   /**
    * Execute after any phase that marks the response as complete.
    */
   private void afterResponseComplete(PhaseId phaseId)
   {
      SessionContext sessionContext = CurrentManager.rootManager().getServices().get(SessionContext.class);
      if (sessionContext.isActive())
      {
         log.trace("Cleaning up the conversation after the " + phaseId + " phase as the response has been marked complete");
         CurrentManager.rootManager().getInstanceByType(ConversationManager.class).cleanupConversation();
      }
      else
      {
         log.trace("Skipping conversation cleanup after the response has been marked complete because the session has been terminated.");
      }
   }

   /**
    * Retrieve the HTTP session from the FacesContext and assign it to the Web
    * Beans HttpSessionManager. Restore the long-running conversation if the
    * conversation id token is present in the request and, in either case,
    * activate the conversation context (long-running or transient).
    */
   private void initiateSessionAndConversation()
   {
      HttpSession session = PhaseHelper.getHttpSession();
      CurrentManager.rootManager().getInstanceByType(HttpSessionManager.class).setSession(session);
      CurrentManager.rootManager().getInstanceByType(ConversationManager.class).beginOrRestoreConversation(PhaseHelper.getConversationId());
      String cid = CurrentManager.rootManager().getInstanceByType(Conversation.class).getId();
      
      ConversationContext conversationContext = CurrentManager.rootManager().getServices().get(ConversationContext.class);
      conversationContext.setBeanStore(new ConversationBeanStore(session, cid));
      conversationContext.setActive(true);
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
