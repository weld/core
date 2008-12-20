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
    * @return An iterator over the remove methods
    */
   public Iterable<MethodDescriptor> getRemoveMethods();

   /**
    * Indicates if the bean is stateless
    * 
    * @return True if stateless, false otherwise
    */
   public boolean isStateless();

   /**
    * Indicates if the bean is a EJB 3.1 Singleton
    * 
    * @return True if the bean is a singleton, false otherwise
    */
   public boolean isSingleton();

   /**
    * Indicates if the EJB is stateful
    * 
    * @return True if the bean is stateful, false otherwise
    */
   public boolean isStateful();

   /**
    * Indicates if the EJB is and MDB
    * 
    * @return True if the bean is an MDB, false otherwise
    */
   public boolean isMessageDriven();

   /**
    * Gets the EJB name
    * 
    * @return The name
    */
   public String getEjbName();
   
   /**
    * @return The JNDI string which can be used to lookup a proxy which 
    * implements all local business interfaces 
    * 
    */
   public String getLocalJndiName();
   
}
