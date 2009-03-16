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

import javax.faces.context.FacesContext;
import javax.inject.AnnotationLiteral;
import javax.servlet.http.HttpSession;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.conversation.ConversationIdName;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * Helper class for JSF related operations
 * 
 * @author Nicklas Karlsson
 * 
 */
public class PhaseHelper
{
   private static LogProvider log = Logging.getLogProvider(PhaseHelper.class);

   private static final String CONVERSATION_PROPAGATION_KEY = "webbeans_conversation_propagation";

   /**
    * Gets a FacesContext instance
    * 
    * @return The current instance
    */
   private static FacesContext context()
   {
      return FacesContext.getCurrentInstance();
   }

   /**
    * Checks if current request is a JSF postback
    * 
    * @return True if postback, false otherwise
    */
   public static boolean isPostback()
   {
      return context().getRenderKit().getResponseStateManager().isPostback(context());
   }

   /**
    * Creates and/or updates the conversation propagation component in the UI
    * view root
    * 
    * @param cid The conversation id to propagate
    */
   public static void propagateConversation(String cid)
   {
      context().getViewRoot().getAttributes().put(CONVERSATION_PROPAGATION_KEY, cid);
      log.debug("Updated propagation component with cid " + cid);
   }

   /**
    * Gets the propagated conversation id parameter from the request
    * 
    * @return The conversation id (or null if not found)
    */
   public static String getConversationIdFromRequest()
   {
      String cidName = CurrentManager.rootManager().getInstanceByType(String.class, new AnnotationLiteral<ConversationIdName>(){});
      String cid = context().getExternalContext().getRequestParameterMap().get(cidName);
      log.trace("Got cid " + cid + " from request");
      return cid;
   }

   /**
    * Gets the propagated conversation id from the view root attribute map
    * 
    * @return The conversation id (or null if not found)
    */
   public static String getConversationIdFromViewRoot()
   {
      String cid = (String) context().getViewRoot().getAttributes().get(CONVERSATION_PROPAGATION_KEY);
      log.trace("Got cid " + cid + " from propagation component");
      return cid;
   }

   /**
    * Gets the propagated conversation id
    * 
    * @return The conversation id (or null if not found)
    */
   public static String getConversationId()
   {
      String cid = null;
      if (isPostback())
      {
         cid = getConversationIdFromViewRoot();
      }
      else
      {
         cid = getConversationIdFromRequest();
      }
      log.debug("Resuming conversation " + cid);
      return cid;
   }

   /**
    * Gets the HTTP session
    * 
    * @return The session
    */
   public static HttpSession getHttpSession()
   {
      return (HttpSession) context().getExternalContext().getSession(true);
   }

   /**
    * Stops conversation propagation through the view root
    */
   public static void stopConversationPropagation()
   {
      context().getViewRoot().getAttributes().remove(CONVERSATION_PROPAGATION_KEY);
   }

}
