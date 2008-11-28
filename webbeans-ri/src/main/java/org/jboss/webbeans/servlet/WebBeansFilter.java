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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Filter for handling request-level events
 * 
 * Delegates work to the ServletLifecycle
 * 
 * @author Pete Muir
 * @author Nicklas Karlsson
 */
public class WebBeansFilter implements Filter
{
   /**
    * Called when the filter is initializes
    * 
    * @param filterConfig The filter configuration
    * @throws ServletException When things go Wrong(tm)
    */
   public void init(FilterConfig filterConfig) throws ServletException
   {
   }

   /**
    * Executes the filter
    * 
    * @param request The request
    * @param response The response
    * @param chain The filter chain
    * 
    * @throws IOException When things go Wrong(tm)
    * @throws ServletException When things go Wrong(tm)
    */
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
   {
      try
      {
         ServletLifecycle.beginRequest((HttpServletRequest) request);
         chain.doFilter(request, response);
      }
      finally
      {
         ServletLifecycle.endRequest((HttpServletRequest) request);
      }
   }

   /**
    * Called when the filter is destroyed
    */
   public void destroy()
   {
   }
}
