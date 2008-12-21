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

package org.jboss.webbeans.util;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.webbeans.ExecutionException;

import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;


/**
 * Provides JNDI access abstraction
 * 
 * @author Pete Muir
 */
public class JNDI
{

   private static final LogProvider log = Logging.getLogProvider(JNDI.class);

   public static InitialContext getInitialContext(Properties props) throws NamingException 
   {
       if (props==null)
       {
           throw new IllegalStateException("JNDI properties not initialized");
       }

       if (log.isDebugEnabled())
       {
           log.debug("JNDI InitialContext properties:" + props);
       }
       
       try {
           return props.size()==0 ?
                   new InitialContext() :
                   new InitialContext(props);
       }
       catch (NamingException e) {
           log.debug("Could not obtain initial context", e);
           throw e;
       }
       
   }
   
   public static InitialContext getInitialContext() throws NamingException 
   {
      return getInitialContext(new Properties());
   }
   
   /**
    * Looks up a object in JNDI
    * 
    * @param name The JNDI name
    * @return The object
    */
   public static Object lookup(String name)
   {
      return lookup(name, Object.class);
   }

   /**
    * Typed JNDI lookup
    * 
    * @param <T> The type
    * @param name The JNDI name
    * @param expectedType The excpected type
    * @return The object
    */
   @SuppressWarnings("unchecked")
   public static <T> T lookup(String name, Class<? extends T> expectedType)
   {
      try
      {
         return (T) getInitialContext().lookup(name);
      }
      catch (NamingException e)
      {
         throw new ExecutionException("Error looking " + name + " up in JNDI", e);
      }
   }

   public static void bind(String key, Object value)
   {
      try
      {
         getInitialContext().bind(key, value);
      }
      catch (NamingException e)
      {
         throw new ExecutionException("Error binding " + value + " to " + key + " in JNDI", e);
      }
   }
   
   public static void bind(String env, String name, Object value)
   {
      try
      {
         Context environment = (Context) getInitialContext().lookup(env);
         environment.bind(name, value);
      }
      catch (NamingException e)
      {
         throw new ExecutionException("Error binding " + value + " to " + name + " in JNDI", e);
      }
   }

}
