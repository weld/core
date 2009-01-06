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

package org.jboss.webbeans.resource;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.webbeans.ExecutionException;

import org.jboss.webbeans.resources.spi.Naming;

/**
 * The default naming provider
 * 
 * @author Pete Muir
 */
public class DefaultNaming implements Naming
{
   private static final long serialVersionUID = 1L;
   // The initial lookup context
   private transient InitialContext initialContext;

   /**
    * Constructor
    */
   public DefaultNaming()
   {
      try
      {
         this.initialContext = new InitialContext();
      }
      catch (NamingException e)
      {
         throw new ExecutionException("Could not obtain InitialContext", e);
      }
   }

   /**
    * Gets the initial context
    * 
    * @return The initial context
    */
   public InitialContext getInitialContext()
   {
      return initialContext;
   }

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
         Context context = initialContext;
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
   @SuppressWarnings("unchecked")
   public <T> T lookup(String name, Class<? extends T> expectedType)
   {
      Object instance;
      try
      {
         instance = initialContext.lookup(name);
      }
      catch (NamingException e)
      {
         throw new ExecutionException("Cannot lookup " + name, e);
      }
      try
      {
         return (T) instance;
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
