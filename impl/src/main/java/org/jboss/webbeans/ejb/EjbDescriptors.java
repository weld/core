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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;

/**
 * EJB descriptors by EJB implementation class or name
 * 
 * @author Pete Muir
 * 
 */
public class EjbDescriptors implements Service, Iterable<InternalEjbDescriptor<?>>
{
   // EJB name -> EJB descriptors map
   private final Map<String, InternalEjbDescriptor<?>> ejbs;
   
   private final Collection<Class<?>> ejbClasses;

   /**
    * Constructor
    */
   public EjbDescriptors()
   {
      this.ejbs = new HashMap<String, InternalEjbDescriptor<?>>();
      this.ejbClasses = new HashSet<Class<?>>();
   }

   /**
    * Gets an iterator to the EJB descriptors for an EJB implementation class
    * 
    * @param beanClass The EJB class
    * @return An iterator
    */
   @SuppressWarnings("unchecked")
   public <T> InternalEjbDescriptor<T> get(String beanName)
   {
      return (InternalEjbDescriptor<T>) ejbs.get(beanName);
   }

   /**
    * Adds an EJB descriptor to the maps
    * 
    * @param ejbDescriptor The EJB descriptor to add
    */
   public <T> void add(EjbDescriptor<T> ejbDescriptor)
   {
      InternalEjbDescriptor<T> internalEjbDescriptor = new InternalEjbDescriptor<T>(ejbDescriptor);
      ejbs.put(ejbDescriptor.getEjbName(), internalEjbDescriptor);
      ejbClasses.add(ejbDescriptor.getBeanClass());
   }

   /**
    * Indicates if there are EJB descriptors available for an EJB implementation
    * class
    * 
    * @param beanClass The class to match
    * @return True if present, otherwise false
    */
   public boolean contains(String beanName)
   {
      return ejbs.containsKey(beanName);
   }
   
   /**
    * Indicates if there are EJB descriptors available for an EJB implementation
    * class
    * 
    * @param beanClass The class to match
    * @return True if present, otherwise false
    */
   public boolean contains(Class<?> beanClass)
   {
      return ejbClasses.contains(beanClass);
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
      ejbs.clear();
   }

   public Iterator<InternalEjbDescriptor<?>> iterator()
   {
      return ejbs.values().iterator();
   }
   
   public void cleanup() {}

}
