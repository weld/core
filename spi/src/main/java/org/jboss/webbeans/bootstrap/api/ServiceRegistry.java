package org.jboss.webbeans.bootstrap.api;

/**
 * A service registry
 * 
 * @author Pete Muir
 *
 */
public interface ServiceRegistry extends Iterable<Service>
{
   
   /**
    * Add a service to bootstrap
    * 
    * @see Service
    * 
    * @param <S> the service type to add
    * @param serviceType the service type to add
    * @param service the service implementation
    */
   public <S extends Service> void add(Class<S> type, S service);
   
   /**
    * Retrieve a service implementation
    * 
    * @param <S> the service type
    * @param serviceType the service type
    * @return the service implementation, or null if none is registered
    */
   public <S extends Service> S get(Class<S> type);
   
   /**
    * Check if a service is registered
    * 
    * @param <S> the service type
    * @param serviceType the service type
    * @return true if a service is registered, otherwise false
    */
   public <S extends Service> boolean contains(Class<S> type);
   
}