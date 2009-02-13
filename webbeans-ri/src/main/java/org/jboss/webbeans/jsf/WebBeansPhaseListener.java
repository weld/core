/*
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

import javax.context.Conversation;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.context.ConversationContext;
import org.jboss.webbeans.conversation.ConversationManager;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * A phase listener for propagating conversation id over postbacks through a
 * hidden component
 * 
 * @author Nicklas Karlsson
 * 
 */
public class WebBeansPhaseListener implements PhaseListener
{
   // The ID/name of the conversation-propagating component
   private static final String CONVERSATION_PROPAGATION_COMPONENT = "webbeans_conversation_propagation";

   private static LogProvider log = Logging.getLogProvider(WebBeansPhaseListener.class);

   public void beforePhase(PhaseEvent phaseEvent)
   {
      if (phaseEvent.getPhaseId().equals(PhaseId.RENDER_RESPONSE))
      {
         beforeRenderReponse();
      }
      else if (phaseEvent.getPhaseId().equals(PhaseId.APPLY_REQUEST_VALUES))
      {
         beforeApplyRequestValues();
      }
   }   

   private void beforeRenderReponse()
   {
      if (JSFHelper.isPostback())
      {
         Conversation conversation = CurrentManager.rootManager().getInstanceByType(Conversation.class);
         if (conversation.isLongRunning())
         {
            JSFHelper.createOrUpdatePropagationComponent(conversation.getId());
         }
         else
         {
            JSFHelper.removePropagationComponent();
         }

      }
   }
   
   private void beforeApplyRequestValues()
   {
      ConversationContext.INSTANCE.setActive(true);
   }   
   
   public void afterPhase(PhaseEvent phaseEvent)
   {
      if (phaseEvent.getPhaseId().equals(PhaseId.RESTORE_VIEW))
      {
         afterRestoreView();
      }
      else if (phaseEvent.getPhaseId().equals(PhaseId.RENDER_RESPONSE))
      {
         afterRenderResponse();
      }
   }   
   
   private void afterRestoreView()
   {
      if (JSFHelper.isPostback())
      {
         CurrentManager.rootManager().getInstanceByType(ConversationManager.class).beginOrRestoreConversation(JSFHelper.getConversationId());
      }
   }

   private void afterRenderResponse()
   {
      ConversationContext.INSTANCE.setActive(false);
   }

   public PhaseId getPhaseId()
   {
      return PhaseId.ANY_PHASE;
   }

}
