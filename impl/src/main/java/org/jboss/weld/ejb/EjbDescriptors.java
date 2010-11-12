/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.ejb;

import static org.jboss.weld.logging.messages.BeanMessage.TOO_MANY_EJBS_FOR_CLASS;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * EJB descriptors by EJB implementation class or name
 * 
 * @author Pete Muir
 * 
 */
public class EjbDescriptors implements Service, Iterable<InternalEjbDescriptor<?>>
{
   // EJB name -> EJB descriptors map
   private final Map<String, InternalEjbDescriptor<?>> ejbByName;
   
   private final SetMultimap<Class<?>, String> ejbByClass;
   
   public static final EjbDescriptors EMPTY = new EjbDescriptors();

   /**
    * Constructor
    */
   public EjbDescriptors()
   {
      this.ejbByName = new HashMap<String, InternalEjbDescriptor<?>>();
      this.ejbByClass = Multimaps.newSetMultimap(new HashMap<Class<?>, Collection<String>>(), new Supplier<Set<String>>()
      {
         
         public Set<String> get()
         {
            return new HashSet<String>();
         }
         
      });
   }

   /**
    * Gets an iterator to the EJB descriptors for an EJB implementation class
    * 
    * @param beanClass The EJB class
    * @return An iterator
    */
   public <T> InternalEjbDescriptor<T> get(String beanName)
   {
      return cast(ejbByName.get(beanName));
   }

   /**
    * Adds an EJB descriptor to the maps
    * 
    * @param ejbDescriptor The EJB descriptor to add
    */
   public <T> void add(EjbDescriptor<T> ejbDescriptor)
   {
      InternalEjbDescriptor<T> internalEjbDescriptor = new InternalEjbDescriptor<T>(ejbDescriptor);
      ejbByName.put(ejbDescriptor.getEjbName(), internalEjbDescriptor);
      ejbByClass.put(ejbDescriptor.getBeanClass(), internalEjbDescriptor.getEjbName());
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
      return ejbByName.containsKey(beanName);
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
      return ejbByClass.containsKey(beanClass);
   }
   
   public InternalEjbDescriptor<?> getUnique(Class<?> beanClass)
   {
      Set<String> ejbs = ejbByClass.get(beanClass);
      if (ejbs.size() > 0)
      {
         throw new IllegalStateException(TOO_MANY_EJBS_FOR_CLASS, beanClass, ejbs);
      }
      else if (ejbs.size() == 0)
      {
         return null;
      }
      else
      {
         return get(ejbs.iterator().next());
      }
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
      ejbByName.clear();
   }

   public Iterator<InternalEjbDescriptor<?>> iterator()
   {
      return ejbByName.values().iterator();
   }
   
   public void cleanup() 
   {
      this.ejbByClass.clear();
      this.ejbByName.clear();
   }

}
