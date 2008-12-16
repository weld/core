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

package org.jboss.webbeans.bootstrap.spi;

import java.lang.reflect.Method;

/**
 * EJB metadata from the EJB descriptor
 * 
 * @author Pete Muir
 *
 * @param <T>
 */
public interface EjbDescriptor<T>
{
   
   /**
    * Gets the EJB type
    * 
    * @return The EJB Bean class
    */
   public Class<T> getType();

   /**
    * Gets the local business interfaces of the EJB
    * 
    * @return An iterator over the local business interfaces
    */
   public Iterable<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces();
   
   /**
    * Gets the remote business interfaces of the EJB
    * 
    * @return An iterator over the remote business interfaces
    */
   public Iterable<BusinessInterfaceDescriptor<?>> getRemoteBusinessInterfaces();
   
   /**
    * Get the remove methods of the EJB
    * 
    * @return An iterator the remove methods
    */
   public Iterable<Method> getRemoveMethods();

   /**
    * The type of EJB
    * @return true if the bean is stateless
    */
   public boolean isStateless();

   /**
    * The type of EJB
    * @return true if the bean is an EJB 3.1 singleton
    */
   public boolean isSingleton();

   /**
    * The type of EJB
    * @return true if the bean is stateful
    */
   public boolean isStateful();

   /**
    * The type of EJB
    * @return true if the bean is an MDB
    */
   public boolean isMessageDriven();

   /**
    * 
    * @return The ejbName
    */
   public String getEjbName();
   
}
