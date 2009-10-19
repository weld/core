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
package org.jboss.weld.jsf;

import static org.jboss.weld.jsf.JsfHelper.getModuleBeanManager;

import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.context.FacesContext;

import org.jboss.weld.conversation.ConversationImpl;

/**
 * <p>
 * A forwarding JSF ViewHandler implementation that produces URLs containing the
 * conversation id query string parameter. All methods except those which
 * produce a URL that need to be enhanced are forwarded to the ViewHandler
 * delegate.
 * </p>
 * 
 * <p>
 * A request parameter was choosen to propagate the conversation because it's
 * the most technology agnostic approach for passing data between requests and
 * allows for the ensuing request to use whatever means necessary (a servlet
 * filter, phase listener, etc) to capture the conversation id and restore the
 * long-running conversation.
 * </p>
 * QUESTION should we do the same for getResourceURL?
 * TODO we should enable a way to disable conversation propagation by URL
 * 
 * @author Dan Allen
 */
public class ConversationAwareViewHandler extends ViewHandlerWrapper
{
   private ViewHandler delegate;

   public ConversationAwareViewHandler(ViewHandler delegate)
   {
      this.delegate = delegate;
   }

   /**
    * Allow the delegate to produce the action URL. If the conversation is
    * long-running, append the conversation id request parameter to the query
    * string part of the URL, but only if the request parameter is not already
    * present.
    *
    * This covers all cases: form actions, link hrefs, Ajax calls, and redirect URLs. 
    * 
    * @see {@link ViewHandler#getActionURL(FacesContext, String)}
    */
   @Override
   public String getActionURL(FacesContext context, String viewId)
   {
      String actionUrl = super.getActionURL(context, viewId);
      ConversationImpl conversation = getModuleBeanManager(context).getInstanceByType(ConversationImpl.class);  
      if (!conversation.isTransient())
      {
         return new FacesUrlTransformer(actionUrl, context).appendConversationIdIfNecessary(conversation.getUnderlyingId()).getUrl();
      }
      else
      {
         return actionUrl;
      }
   }

   /**
    * @see {@link ViewHandlerWrapper#getWrapped()}
    */
   @Override
   public ViewHandler getWrapped()
   {
      return delegate;
   }

}
