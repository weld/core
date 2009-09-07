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

import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.util.ApiAbstraction;

/**
 * Abstraction for classes in the Servlet API
 * 
 * @author Pete Muir
 */
public class ServletApiAbstraction extends ApiAbstraction implements Service
{
   
   public final Class<?> SERVLET_CLASS;
   public final Class<?> FILTER_CLASS;
   public final Class<?> SERVLET_CONTEXT_LISTENER_CLASS;
   public final Class<?> HTTP_SESSION_LISTENER_CLASS;
   public final Class<?> SERVLET_REQUEST_LISTENER_CLASS;

   /**
    * Constructor
    * 
    * @param resourceLoader The root resource loader
    */
   public ServletApiAbstraction(ResourceLoader resourceLoader)
   {
      super(resourceLoader);
      SERVLET_CLASS = classForName("javax.servlet.Servlet");
      FILTER_CLASS = classForName("javax.servlet.Filter");
      SERVLET_CONTEXT_LISTENER_CLASS = classForName("javax.servlet.ServletContextListener");
      HTTP_SESSION_LISTENER_CLASS = classForName("javax.servlet.http.HttpSessionListener");
      SERVLET_REQUEST_LISTENER_CLASS = classForName("javax.servlet.ServletRequestListener");
   }
   
   public void cleanup() {}
   
   
}
