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
import java.net.URL;

import javax.servlet.ServletContext;

import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceLoadingException;
import org.jboss.webbeans.util.EnumerationIterable;

/**
 * A resource loader based on a servlet context
 * 
 * @author Pete Muir
 *
 */
public class ServletContextResourceLoader implements ResourceLoader
{
   // The servlet context
   private final ServletContext servletContext;
   
   /**
    * Constructor
    * 
    * @param servletContext The servlet context
    */
   public ServletContextResourceLoader(ServletContext servletContext)
   {
      this.servletContext = servletContext;
   }

   /**
    * Creates a class with a given name from the servlet contexts classloader
    * 
    * @param The FQCN of the class
    * @return The class
    * 
    * @see org.jboss.webbeans.resources.spi.ResourceLoader#classForName(String)
    */
   public Class<?> classForName(String name)
   {
      try
      {
         return servletContext.getClass().getClassLoader().loadClass(name);
      }
      catch (ClassNotFoundException e)
      {
         throw new ResourceLoadingException(e);
      }
      catch (NoClassDefFoundError e)
      {
         throw new ResourceLoadingException(e);
      }
   }

   /**
    *  @see org.jboss.webbeans.resources.spi.ResourceLoader#getResource(String)
    */
   public URL getResource(String name)
   {
      URL resource = getResourceFromServletContext(name);
      if (resource == null)
      {
         String stripped = name.startsWith("/") ? name.substring(1) : name;
         resource = servletContext.getClass().getClassLoader().getResource(stripped);
      }
      return resource;
   }
   
   /**
    * Gets an resource from the classloader of the servlet context
    * 
    * @param name The name of the resource
    * @return An URL to the resource
    */
   private URL getResourceFromServletContext(String name)
   {
      try
      {
         return servletContext.getResource(name);
      }
      catch (Exception e) 
      {
         return null;
      }
   }
   
   /**
    *  @see org.jboss.webbeans.resources.spi.ResourceLoader#getResource(String)
    */
   public Iterable<URL> getResources(String name)
   {
      try
      {
         // TODO load resources from servlet context!
         return new EnumerationIterable<URL>(servletContext.getClass().getClassLoader().getResources(name));
      }
      catch (IOException e)
      {
         throw new ResourceLoadingException("Error loading resources for " + name, e);
      }
   }
   
}
