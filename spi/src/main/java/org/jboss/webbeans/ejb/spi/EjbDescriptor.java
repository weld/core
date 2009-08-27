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

package org.jboss.webbeans.ejb.spi;

import java.lang.reflect.Method;
import java.util.Collection;


/**
 * EJB metadata from the EJB descriptor
 * 
 * @author Pete Muir
 *
 * @param <T> the bean type
 */
public interface EjbDescriptor<T>
{
   
   /**
    * Gets the EJB type
    * 
    * @return The EJB Bean class
    */
   public Class<T> getBeanClass();

   /**
    * Gets the local business interfaces of the EJB
    * 
    * @return An iterator over the local business interfaces
    */
   public Collection<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces();
   
   /**
    * Get the EJB name
    * 
    * @return
    */
   public String getEjbName();
   
   /**
    * Get the remove methods of the EJB
    * 
    * @return An iterator over the remove methods
    */
   public Collection<Method> getRemoveMethods();

   /**
    * Indicates if the bean is a stateless session bean 
    * 
    * @return True if stateless, false otherwise
    */
   public boolean isStateless();

   /**
    * Indicates if the bean is a EJB 3.1 Singleton session bean
    * 
    * @return True if the bean is a singleton, false otherwise
    */
   public boolean isSingleton();

   /**
    * Indicates if the EJB is a stateful session bean
    * 
    * @return True if the bean is stateful, false otherwise
    */
   public boolean isStateful();

   /**
    * Indicates if the EJB is an MDB
    * 
    * @return True if the bean is an MDB, false otherwise
    */
   public boolean isMessageDriven();
   
}
