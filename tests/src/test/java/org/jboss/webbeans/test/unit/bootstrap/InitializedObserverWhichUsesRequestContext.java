package org.jboss.webbeans.test.unit.bootstrap;

import javax.enterprise.inject.Current;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.event.Observes;

class InitializedObserverWhichUsesRequestContext
{
   
   public static String name;
   
   @Current Tuna tuna;
   
   public void observeInitialized(@Observes AfterBeanDiscovery event)
   {
      name = tuna.getName();
   }
   
}
