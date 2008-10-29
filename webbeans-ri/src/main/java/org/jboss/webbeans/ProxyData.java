package org.jboss.webbeans;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Manager;

public class ProxyData
{
   private Bean<?> bean;
   private Manager manager;
   
   public ProxyData(Bean<?> bean, Manager manager)
   {
      this.bean = bean;
      this.manager = manager;
   }

   public Bean<?> getBean()
   {
      return bean;
   }

   public Manager getManager()
   {
      return manager;
   }
   
}
