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

import javax.webbeans.CreationException;
import javax.webbeans.Current;
import javax.webbeans.Initializer;
import javax.webbeans.Standard;
import javax.webbeans.manager.EnterpriseBeanLookup;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bootstrap.spi.EjbDescriptor;
import org.jboss.webbeans.util.JNDI;

/**
 * Provides lookup and metadata registration services for EJBs
 * 
 * @author Pete Muir
 * @see java.webbeans.manager.EnterpriseBeanLookup
 */
@Standard
public class DefaultEnterpriseBeanLookup implements EnterpriseBeanLookup
{
   
   private ManagerImpl manager;
   
   @Initializer
   public DefaultEnterpriseBeanLookup(@Current ManagerImpl manager)
   {
      this.manager = manager;
   }

   /**
    * Looks up and EJB based on the name
    * 
    * @param ejbName The EJB name
    * @return The EJB reference
    */
   public Object lookup(String ejbName)
   {
      if (ejbName == null)
      {
         throw new NullPointerException("No EJB name supplied for lookup");
      }
      return lookup(manager.getEjbDescriptorCache().get(ejbName));
   }

   /**
    * Looks up an EJB based on the EJB descriptor
    * 
    * @param <T> The type of the EJB
    * @param ejbDescriptor The EJB descriptor
    * @return The EJB reference
    */
   @SuppressWarnings("unchecked")
   public <T> T lookup(EjbDescriptor<T> ejbDescriptor)
   {
      try
      {
         // TODO Implement enterprise proxies and select the correct jndiName
         return (T) JNDI.lookup(ejbDescriptor.getLocalJndiName());
      }
      catch (Exception e)
      {
         throw new CreationException("could not find the name in JNDI " + ejbDescriptor.getLocalJndiName(), e);
      }
   }

}
