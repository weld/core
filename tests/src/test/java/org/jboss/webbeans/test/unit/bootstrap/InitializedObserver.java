package org.jboss.webbeans.test.unit.bootstrap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;


public class InitializedObserver implements Extension
{
   
   public static boolean observered;
   
   public void observeInitialized(@Observes AfterBeanDiscovery event)
   {
      observered = true;
   }
   
}
