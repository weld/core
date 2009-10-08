package org.jboss.weld.bootstrap.api.helpers;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;

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
   
   public Iterator<Service> iterator()
   {
      return delegate().iterator();
   }

   public void addAll(Collection<Entry<Class<? extends Service>, Service>> services)
   {
      delegate().addAll(services);
   }

   public Set<Entry<Class<? extends Service>, Service>> entrySet()
   {
      return delegate().entrySet();
   }
   
   public void cleanup()
   {
      delegate().cleanup();
   }
   
}
