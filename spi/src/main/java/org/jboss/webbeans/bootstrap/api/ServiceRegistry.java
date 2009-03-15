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
package org.jboss.webbeans.bootstrap.api;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry for services
 * 
 * @author Pete Muir
 *
 */
public class ServiceRegistry
{
   
   private final Map<Class<? extends Service>, Service> services;
   
   public ServiceRegistry()
   {
      this.services = new HashMap<Class<? extends Service>, Service>();
   }
   
   /**
    * Add a service to bootstrap
    * 
    * @see Service
    * 
    * @param <S> the service type to add
    * @param serviceType the service type to add
    * @param service the service implementation
    */
   public <S extends Service> void add(java.lang.Class<S> type, S service) 
   {
      if (service == null)
      {
         services.remove(type);
      }
      else
      {
         services.put(type, service);
      }
   }
   
   /**
    * Retrieve a service implementation
    * 
    * @param <S> the service type
    * @param serviceType the service type
    * @return the service implementation, or null if none is registered
    */
   @SuppressWarnings("unchecked")
   public <S extends Service> S get(Class<S> type)
   {
      return (S) services.get(type);
   }
   
   /**
    * Check if a service is registered
    * 
    * @param <S> the service type
    * @param serviceType the service type
    * @return true if a service is registered, otherwise false
    */
   public <S extends Service> boolean contains(Class<S> type)
   {
      return services.containsKey(type);
   }   
   
}
