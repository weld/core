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

package org.jboss.webbeans.resources.spi.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.ExecutionException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.webbeans.resources.spi.NamingContext;

/**
 * Provides common functionality required by a NamingContext
 * 
 * @author Pete Muir
 *
 */
public abstract class AbstractNamingContext implements NamingContext
{
   
   public abstract Context getContext();
   
   /**
    * Binds in item to JNDI
    * 
    * @param key The key to bind under
    * @param value The value to bind
    */
   public void bind(String key, Object value)
   {
      try
      {
         List<String> parts = splitIntoContexts(key);
         Context context = getContext();
         for (int i = 0; i < parts.size() - 1; i++)
         {
            context = (Context) context.lookup(parts.get(i));
         }
         context.bind(parts.get(parts.size() - 1), value);
      }
      catch (NamingException e)
      {
         throw new ExecutionException("Cannot bind " + value + " to " + key, e);
      }
   }

   /**
    * Lookup an item from JNDI
    * 
    * @param name The key
    * @param expectedType The expected return type
    * @return The found item
    */
   public <T> T lookup(String name, Class<? extends T> expectedType)
   {
      Object instance;
      try
      {
         instance = getContext().lookup(name);
      }
      catch (NamingException e)
      {
         throw new ExecutionException("Cannot lookup " + name, e);
      }
      try
      {
         return expectedType.cast(instance);
      }
      catch (ClassCastException e)
      {
         throw new ExecutionException(instance + " not of expected type " + expectedType, e);
      }
   }
   
   private static List<String> splitIntoContexts(String key)
   {
      List<String> parts = new ArrayList<String>();
      for (String part : key.split("/"))
      {
         parts.add(part);
      }
      return parts;
   }
   
}