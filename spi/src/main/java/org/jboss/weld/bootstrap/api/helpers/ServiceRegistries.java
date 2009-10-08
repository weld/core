package org.jboss.weld.bootstrap.api.helpers;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;

public class ServiceRegistries
{
   
   private ServiceRegistries() {}
   
   public static ServiceRegistry unmodifiableServiceRegistry(final ServiceRegistry serviceRegistry)
   {
      return new ForwardingServiceRegistry()
      {
         
         public <S extends Service> void add(java.lang.Class<S> type, S service) 
         {
            throw new UnsupportedOperationException("This service registry is unmodifiable");
         }
         
         @Override
         protected ServiceRegistry delegate()
         {
            return serviceRegistry;
         }
         
      };
   }
   
}
