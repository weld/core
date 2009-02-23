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
package org.jboss.webbeans.servlet;

import java.io.IOException;

import javax.context.Conversation;
import javax.faces.context.FacesContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.webbeans.CurrentManager;

/**
 * Filter for handling conversation propagation over redirects
 * 
 * @author Nicklas Karlsson
 * 
 */
// TODO: Quick and dirty, not for actual usage yet ;-)
public class WebBeansServletFilter implements Filter
{

   private class RedirectUrl
   {
      private String URL;
      private FacesContext context;

      public RedirectUrl(String URL)
      {
         this.URL = URL;
         context = FacesContext.getCurrentInstance();
      }

      public RedirectUrl appendCid(String cid)
      {
         URL = URL + (URL.indexOf("?") > 0 ? "&" : "?") + "cid=" + cid;
         return this;
      }

      public RedirectUrl getRedirectView()
      {
         String requestPath = context.getExternalContext().getRequestContextPath();
         URL = URL.substring(URL.indexOf(requestPath) + requestPath.length());
         return this;
      }

      public RedirectUrl getActionUrl()
      {
         URL = context.getApplication().getViewHandler().getActionURL(context, URL);
         return this;
      }

      public String encode()
      {
         return context.getExternalContext().encodeActionURL(URL);
      }
   }

   public void destroy()
   {
   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
   {
      chain.doFilter(request, wrapResponse((HttpServletResponse) response));
   }

   private ServletResponse wrapResponse(HttpServletResponse response)
   {
      return new HttpServletResponseWrapper(response)
      {
         @Override
         public void sendRedirect(String path) throws IOException
         {
            Conversation conversation = CurrentManager.rootManager().getInstanceByType(Conversation.class);
            if (conversation.isLongRunning())
            {
               path = new RedirectUrl(path).getRedirectView().getActionUrl().appendCid(conversation.getId()).encode();
            }
            super.sendRedirect(path);
         }
      };
   }

   public void init(FilterConfig config) throws ServletException
   {
   }

}
