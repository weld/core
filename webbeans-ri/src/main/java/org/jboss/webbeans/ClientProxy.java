package org.jboss.webbeans;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Manager;

public class ClientProxy<T>
{
   private Bean<T> bean;
   private Manager manager;
   
   public ClientProxy(Bean<T> bean, Manager manager)
   {
      this.bean = bean;
      this.manager = manager;
   }
   
   public T getInstance() 
   {
      Context context = manager.getContext(bean.getScopeType());
      T instance = context.get(bean, true);
      // wrap in proxy here
      return instance;
   }

}
