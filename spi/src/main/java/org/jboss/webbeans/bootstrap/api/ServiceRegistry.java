package org.jboss.webbeans.bootstrap.api;

import java.util.HashMap;
import java.util.Map;


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
      services.put(type, service);
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
