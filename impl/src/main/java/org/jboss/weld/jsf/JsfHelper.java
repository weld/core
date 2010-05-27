/*
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

import static org.jboss.weld.logging.Category.JSF;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.JsfMessage.FOUND_CONVERSATION_FROM_REQUEST;
import static org.jboss.weld.logging.messages.JsfMessage.IMPROPER_ENVIRONMENT;
import static org.jboss.weld.logging.messages.JsfMessage.RESUMING_CONVERSATION;
import static org.jboss.weld.servlet.BeanProvider.conversationIdName;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.jboss.weld.exceptions.IllegalStateException;
import org.slf4j.cal10n.LocLogger;

/**
 * Helper class for JSF related operations
 * 
 * @author Nicklas Karlsson
 * @author Dan Allen
 */
public class JsfHelper
{
   
   private static final LocLogger log = loggerFactory().getLogger(JSF);

   /**
    * Gets the propagated conversation id parameter from the request
    * 
    * @return The conversation id (or null if not found)
    */
   public static String getConversationIdFromRequest(FacesContext facesContext)
   {
      String cidName = conversationIdName(getServletContext(facesContext));
      String cid = facesContext.getExternalContext().getRequestParameterMap().get(cidName);
      log.trace(FOUND_CONVERSATION_FROM_REQUEST, cid);
      return cid;
   }

   /**
    * Gets the propagated conversation id.
    * 
    * @return The conversation id (or null if not found)
    */
   public static String getConversationId(FacesContext facesContext)
   {
      String cid = getConversationIdFromRequest(facesContext);
      log.debug(RESUMING_CONVERSATION, cid);
      return cid;
   }
   
   /**
    * Gets the HTTP session
    * 
    * @return The session
    */
   public static HttpSession getHttpSession(FacesContext facesContext)
   {
      Object session = facesContext.getExternalContext().getSession(true);
      if (session instanceof HttpSession)
      {
         return (HttpSession) session;
      }
      else
      {
         return null;
      }
   }
   
   public static ServletContext getServletContext(FacesContext facesContext)
   {
      if (facesContext.getExternalContext().getContext() instanceof ServletContext)
      {
         return (ServletContext) facesContext.getExternalContext().getContext();
      }
      else
      {
         throw new IllegalStateException(IMPROPER_ENVIRONMENT);
      }
   }

}
