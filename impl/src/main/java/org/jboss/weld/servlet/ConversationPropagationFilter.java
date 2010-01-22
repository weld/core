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
package org.jboss.weld.servlet;

import static org.jboss.weld.servlet.BeanProvider.conversation;
import static org.jboss.weld.servlet.BeanProvider.conversationManager;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.weld.conversation.ConversationImpl;
import org.jboss.weld.jsf.FacesUrlTransformer;

/**
 * <p>A Filter for handling conversation propagation over redirects.</p>
 * 
 * <p>This fiter intercepts the call to {@link HttpServletResponse#sendRedirect(String)} and
 * appends the conversation id request parameter to the URL if the conversation is long-running,
 * but only if the request parameter is not already present.</p>
 * 
 * FIXME This filter is specifically for JSF and should be repackaged or split up to support non-JSF environments.
 * 
 * @author Nicklas Karlsson
 */
public class ConversationPropagationFilter implements Filter
{
   
   private ServletContext ctx;
   
   public void init(FilterConfig config) throws ServletException
   {
      ctx = config.getServletContext();
   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
   {
      if (request instanceof HttpServletRequest && response instanceof HttpServletResponse)
      {
         response = wrapResponse((HttpServletResponse) response, ((HttpServletRequest) request).getContextPath());
      }
      chain.doFilter(request, response);
   }
   
   public void destroy()
   {
   }

   private ServletResponse wrapResponse(HttpServletResponse response, final String requestPath)
   {
      return new HttpServletResponseWrapper(response)
      {
         @Override
         public void sendRedirect(String path) throws IOException
         {
            ConversationImpl conversation = conversation(ctx);
            if (!conversation.isTransient())
            {
               path = new FacesUrlTransformer(path, FacesContext.getCurrentInstance()).toRedirectViewId().toActionUrl().appendConversationIdIfNecessary(conversation.getUnderlyingId()).encode();
               conversationManager(ctx).cleanupConversation();
            }
            super.sendRedirect(path);
         }
      };
   }

}
