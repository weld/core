package org.jboss.webbeans.bootstrap.api.helpers;

import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;

public abstract class ForwardingServiceRegistry implements ServiceRegistry
{
   
   protected abstract ServiceRegistry delegate();
   
   public <S extends Service> void add(Class<S> type, S service)
   {
      delegate().add(type, service);
   }
   
   public <S extends Service> boolean contains(Class<S> type)
   {
      return delegate().contains(type);
   }
   
   public <S extends Service> S get(Class<S> type)
   {
      return delegate().get(type);
   }
   
}
