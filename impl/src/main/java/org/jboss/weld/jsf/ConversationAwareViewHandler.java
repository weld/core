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

import javax.enterprise.context.Conversation;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Instance;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.context.FacesContext;

import org.jboss.weld.Container;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.http.HttpConversationContext;

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
 * 
 * @author Dan Allen
 * @author Pete Muir
 */
public class ConversationAwareViewHandler extends ViewHandlerWrapper
{

   private final ViewHandler delegate;
   private final Instance<Context> context;

   public ConversationAwareViewHandler(ViewHandler delegate)
   {
      this.delegate = delegate;
      Container container = Container.instance();
      this.context = container.deploymentManager().instance().select(Context.class);
   }

   /**
    * Allow the delegate to produce the action URL. If the conversation is
    * long-running, append the conversation id request parameter to the query
    * string part of the URL, but only if the request parameter is not already
    * present.
    * 
    * This covers form actions Ajax calls, and redirect URLs (which we want) and
    * link hrefs (which we don't)
    * 
    * @see {@link ViewHandler#getActionURL(FacesContext, String)}
    */
   @Override
   public String getActionURL(FacesContext facesContext, String viewId)
   {
      ConversationContext conversationContext = context.select(HttpConversationContext.class).get();
      String actionUrl = super.getActionURL(facesContext, viewId);
      Conversation conversation = conversationContext.getCurrentConversation();
      if (!conversation.isTransient())
      {
         return new FacesUrlTransformer(actionUrl, facesContext).appendConversationIdIfNecessary(conversationContext.getParameterName(), conversation.getId()).getUrl();
      }
      else
      {
         return actionUrl;
      }
   }

   @Override
   public ViewHandler getWrapped()
   {
      return delegate;
   }

}
