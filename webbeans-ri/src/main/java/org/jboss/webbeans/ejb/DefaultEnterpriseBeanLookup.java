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

package org.jboss.webbeans.ejb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.webbeans.CreationException;
import javax.webbeans.Standard;
import javax.webbeans.manager.EnterpriseBeanLookup;

import org.jboss.webbeans.util.JNDI;
import org.jboss.webbeans.util.Strings;

import com.google.common.collect.ForwardingMap;

/**
 * Provides lookup and metadata registration services for EJBs
 * 
 * @author Pete Muir
 * @see java.webbeans.manager.EnterpriseBeanLookup
 */
@Standard
public class DefaultEnterpriseBeanLookup implements EnterpriseBeanLookup
{
   /**
    * An EJB name -> metadata map
    */
   private class EjbMetaDataMap extends ForwardingMap<String, EjbMetaData<?>>
   {
      private Map<String, EjbMetaData<?>> delegate;

      public EjbMetaDataMap()
      {
         delegate = new ConcurrentHashMap<String, EjbMetaData<?>>();
      }

      @Override
      protected Map<String, EjbMetaData<?>> delegate()
      {
         return delegate;
      }

      @Override
      public String toString()
      {
         return Strings.mapToString("EjbMetaDataMap (EJB name -> metadata): ", delegate);
      }
   }

   // A map from EJB name to EJB metadata
   private EjbMetaDataMap ejbMetaDataMap = new EjbMetaDataMap();

   /**
    * Looks up and EJB based on the name
    * 
    * Gets the EJB metadata and calls helper method
    * 
    * @param ejbName The EJB name
    * @return The EJB local home interface
    * @see javax.webbeans.manager.EnterpriseBeanLookup#lookup(String)
    */
   public Object lookup(String ejbName)
   {
      return lookup(ejbMetaDataMap.get(ejbName));
   }

   /**
    * Looks up an EJB
    * 
    * First tried the EJB link JNDI name, if available, then the default JNDI
    * name. Throws an CreationException if it isn't found.
    * 
    * @param <T> The type of the EJB
    * @param ejbMetaData The EJB metadata
    * @return The EJB local interface
    */
   public static <T> T lookup(EjbMetaData<T> ejbMetaData)
   {
      try
      {
         if (ejbMetaData.getEjbLinkJndiName() != null)
         {
            return JNDI.lookup(ejbMetaData.getEjbLinkJndiName(), ejbMetaData.getType());
         }
         return JNDI.lookup(ejbMetaData.getDefaultJndiName(), ejbMetaData.getType());
      }
      catch (Exception e)
      {
         throw new CreationException("could not find the EJB in JNDI", e);
      }
   }

   // TODO: this method needs to get called at startup
   /**
    * Creates and registers EJB metadata for a class
    * 
    * @param clazz The EJB class
    * @return the EJB metadata
    */
   public <T> EjbMetaData<T> registerEjbMetaData(Class<T> clazz)
   {
      EjbMetaData<T> ejbMetaData = new EjbMetaData<T>(clazz);
      ejbMetaDataMap.put(ejbMetaData.getEjbName(), ejbMetaData);
      return ejbMetaData;
   }
}
