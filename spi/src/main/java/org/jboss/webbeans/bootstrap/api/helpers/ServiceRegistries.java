package org.jboss.webbeans.bootstrap.api.helpers;

import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;

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
