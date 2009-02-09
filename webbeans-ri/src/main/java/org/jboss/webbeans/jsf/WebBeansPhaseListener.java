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
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.conversation.ConversationManager;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.servlet.ServletLifecycle;

/**
 * A phase listener for propagating conversation id over postbacks through a hidden component
 *  
 * @author Nicklas Karlsson
 *
 */
public class WebBeansPhaseListener implements PhaseListener
{
   // The ID/name of the conversation-propagating component
   private static final String CONVERSATION_PROPAGATION_COMPONENT = "jboss_org_webbeans_conversation_propagation";

   private static LogProvider log = Logging.getLogProvider(ServletLifecycle.class);

   /**
    * Indicates if we are in a JSF postback or not
    *  
    * @return True if postback, false otherwise
    */
   private boolean isPostback()
   {
      return FacesContext.getCurrentInstance().getRenderKit().getResponseStateManager().isPostback(FacesContext.getCurrentInstance());
   }

   public void afterPhase(PhaseEvent phaseEvent)
   {
      // If we are restoring a view and we are in a postback
      if (phaseEvent.getPhaseId().equals(PhaseId.RESTORE_VIEW) && isPostback())
      {
         log.trace("Processing after RESTORE_VIEW phase");
         HtmlInputHidden propagationComponent = getPropagationComponent(phaseEvent.getFacesContext().getViewRoot());
         // Resume the conversation if the propagation component can be found
         if (propagationComponent != null)
         {
            log.trace("Propagation component found");
            String cid = propagationComponent.getValue().toString();
            ConversationManager conversationManager = CurrentManager.rootManager().getInstanceByType(ConversationManager.class);
            conversationManager.beginOrRestoreConversation(cid);
         }
      }
   }

   public void beforePhase(PhaseEvent phaseEvent)
   {
      if (phaseEvent.getPhaseId().equals(PhaseId.RENDER_RESPONSE) && isPostback())
      {
         // If we are rendering the response from a postback
         log.trace("Processing after RENDER_RESPONSE phase");
         Conversation conversation = CurrentManager.rootManager().getInstanceByType(Conversation.class);
         // If we are in a long-running conversation, create or update the conversation id
         // in the propagation component in the view root
         if (conversation.isLongRunning())
         {
            log.trace("Updating propagation for " + conversation);
            createOrUpdatePropagationComponent(phaseEvent.getFacesContext(), conversation.getId());
         }
         else
         {
            // Otherwise, remove the component from the view root
            log.trace("Removing propagation for " + conversation);
            removePropagationComponent(phaseEvent.getFacesContext().getViewRoot());
         }
      }
   }

   /**
    * Gets the conversation propagation component
    * 
    * @param viewRoot The view root to search in
    * @return The component, or null if it's not present
    */
   private HtmlInputHidden getPropagationComponent(UIViewRoot viewRoot)
   {
      return (HtmlInputHidden) viewRoot.findComponent(CONVERSATION_PROPAGATION_COMPONENT);
   }

   /**
    * Creates or updates the conversation propagation component in the view root
    * 
    * @param facesContext The faces context
    * @param cid The conversation id to propagate
    */
   private void createOrUpdatePropagationComponent(FacesContext facesContext, String cid)
   {
      HtmlInputHidden propagationComponent = getPropagationComponent(facesContext.getViewRoot());
      // Creates the component if it can't be found
      if (propagationComponent == null)
      {
         propagationComponent = (HtmlInputHidden) facesContext.getApplication().createComponent(HtmlInputHidden.COMPONENT_TYPE);
         propagationComponent.setId(CONVERSATION_PROPAGATION_COMPONENT);
         facesContext.getViewRoot().getChildren().add(propagationComponent);
      }
      propagationComponent.setValue(cid);
   }

   /**
    * Removes the conversation propagation from the view root (if present)
    * 
    * @param viewRoot The view root to remove the component from
    */
   private void removePropagationComponent(UIViewRoot viewRoot)
   {
      HtmlInputHidden propagationComponent = getPropagationComponent(viewRoot);
      if (propagationComponent != null)
      {
         viewRoot.getChildren().remove(propagationComponent);
      }
   }

   public PhaseId getPhaseId()
   {
      return PhaseId.ANY_PHASE;
   }

}
