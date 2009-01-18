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
package org.jboss.webbeans.bootstrap;

import java.io.IOException;
import java.net.URL;

import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceLoadingException;
import org.jboss.webbeans.util.EnumerationIterable;

/**
 * A simple resource loader.
 * 
 * Uses {@link SimpleResourceLoader}'s classloader if the Thread Context 
 * Classloader isn't available
 * 
 * @author Pete Muir
 *
 */
public class SimpleResourceLoader implements ResourceLoader
{
   
   public Class<?> classForName(String name)
   {
      
      try
      {
         if (Thread.currentThread().getContextClassLoader() != null)
         {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
         }
         else
         {
            return Class.forName(name);
         }
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
   
   public URL getResource(String name)
   {
      if (Thread.currentThread().getContextClassLoader() != null)
      {
         return Thread.currentThread().getContextClassLoader().getResource(name);
      }
      else
      {
         return getClass().getResource(name);
      }
   }
   
   public Iterable<URL> getResources(String name)
   {
      try
      {
         if (Thread.currentThread().getContextClassLoader() != null)
         {
            return new EnumerationIterable<URL>(Thread.currentThread().getContextClassLoader().getResources(name));
         }
         else
         {
            return new EnumerationIterable<URL>(getClass().getClassLoader().getResources(name));
         }
      }
      catch (IOException e)
      {
         throw new ResourceLoadingException(e);
      }
   }
   
}
