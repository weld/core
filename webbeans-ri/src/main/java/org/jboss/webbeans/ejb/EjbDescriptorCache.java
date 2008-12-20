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

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jboss.webbeans.bootstrap.spi.EjbDescriptor;

/**
 * EJB descriptors by EJB implementation class or name
 * 
 * @author Pete Muir
 * 
 */
public class EjbDescriptorCache implements Serializable
{

   // EJB name -> EJB descriptor map
   private ConcurrentMap<String, EjbDescriptor<?>> ejbsByName;
   // EJB implementation class -> EJB descriptors map
   private ConcurrentMap<Class<?>, Set<EjbDescriptor<?>>> ejbsByBeanClass;

   /**
    * Constructor
    */
   public EjbDescriptorCache()
   {
      this.ejbsByName = new ConcurrentHashMap<String, EjbDescriptor<?>>();
      this.ejbsByBeanClass = new ConcurrentHashMap<Class<?>, Set<EjbDescriptor<?>>>();
   }

   /**
    * Gets the EJB descriptor for a given name
    * 
    * @param ejbName The EJB name
    * @return The EJB descriptor
    */
   public EjbDescriptor<?> get(String ejbName)
   {
      return ejbsByName.get(ejbName);
   }

   /**
    * Gets an iterator to the EJB descriptors for an EJB implementation class
    * 
    * @param beanClass The EJB class
    * @return An iterator
    */
   public <T> Iterable<EjbDescriptor<T>> get(Class<T> beanClass)
   {
      return (Iterable) ejbsByBeanClass.get(beanClass);
   }

   /**
    * Adds an EJB descriptor to the maps
    * 
    * @param ejbDescriptor The EJB descriptor to add
    */
   public void add(EjbDescriptor<?> ejbDescriptor)
   {
      ejbsByName.put(ejbDescriptor.getEjbName(), ejbDescriptor);
      ejbsByBeanClass.putIfAbsent(ejbDescriptor.getType(), new CopyOnWriteArraySet<EjbDescriptor<?>>());
      ejbsByBeanClass.get(ejbDescriptor.getType()).add(ejbDescriptor);
   }

   /**
    * Indicates if there are EJB descriptors available for an EJB name
    * 
    * @param ejbName The EJB name to match
    * @return True if present, otherwise false
    */
   public boolean containsKey(String ejbName)
   {
      return ejbsByName.containsKey(ejbName);
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
   
   public void clear()
   {
      ejbsByBeanClass.clear();
      ejbsByName.clear();
   }
   
   @Override
   public String toString()
   {
      return ejbsByBeanClass + "\n" + ejbsByName;
   }

}
