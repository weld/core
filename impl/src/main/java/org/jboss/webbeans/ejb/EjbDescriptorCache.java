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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jboss.webbeans.ejb.spi.EjbDescriptor;

/**
 * EJB descriptors by EJB implementation class or name
 * 
 * @author Pete Muir
 * 
 */
public class EjbDescriptorCache
{
   // EJB implementation class -> EJB descriptors map
   private Map<Class<?>, Set<InternalEjbDescriptor<?>>> ejbsByBeanClass;

   /**
    * Constructor
    */
   public EjbDescriptorCache()
   {
      this.ejbsByBeanClass = new HashMap<Class<?>, Set<InternalEjbDescriptor<?>>>();
   }

   /**
    * Gets an iterator to the EJB descriptors for an EJB implementation class
    * 
    * @param beanClass The EJB class
    * @return An iterator
    */
   @SuppressWarnings("unchecked")
   public <T> Iterable<InternalEjbDescriptor<T>> get(Class<T> beanClass)
   {
      return (Iterable) ejbsByBeanClass.get(beanClass);
   }

   /**
    * Adds an EJB descriptor to the maps
    * 
    * @param ejbDescriptor The EJB descriptor to add
    */
   public <T> void add(EjbDescriptor<T> ejbDescriptor)
   {
      InternalEjbDescriptor<T> internalEjbDescriptor = new InternalEjbDescriptor<T>(ejbDescriptor);
      if (!ejbsByBeanClass.containsKey(ejbDescriptor.getBeanClass()))
      {
         ejbsByBeanClass.put(ejbDescriptor.getBeanClass(), new CopyOnWriteArraySet<InternalEjbDescriptor<?>>());  
      }
      ejbsByBeanClass.get(ejbDescriptor.getBeanClass()).add(internalEjbDescriptor);
   }

   /**
    * Indicates if there are EJB descriptors available for an EJB implementation
    * class
    * 
    * @param beanClass The class to match
    * @return True if present, otherwise false
    */
   public boolean containsKey(Class<?> beanClass)
   {
      return ejbsByBeanClass.containsKey(beanClass);
   }

   /**
    * Adds all EJB descriptors to the maps
    * 
    * @param ejbDescriptors The descriptors to add
    */
   public void addAll(Iterable<EjbDescriptor<?>> ejbDescriptors)
   {
      for (EjbDescriptor<?> ejbDescriptor : ejbDescriptors)
      {
         add(ejbDescriptor);
      }
   }

   /**
    * Clears both maps
    */
   public void clear()
   {
      ejbsByBeanClass.clear();
   }

   @Override
   public String toString()
   {
      return "EJB Descriptor cache has indexed " + ejbsByBeanClass.size() + " EJBs by class";
   }

}
