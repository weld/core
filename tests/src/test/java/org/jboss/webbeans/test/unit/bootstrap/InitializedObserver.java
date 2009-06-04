package org.jboss.webbeans.test.unit.bootstrap;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.event.Observes;


class InitializedObserver
{
   
   public static boolean observered;
   
   public void observeInitialized(@Observes AfterBeanDiscovery event)
   {
      observered = true;
   }
   
}
