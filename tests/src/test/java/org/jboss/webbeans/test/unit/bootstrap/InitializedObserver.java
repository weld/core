package org.jboss.webbeans.test.unit.bootstrap;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.event.Observes;

class InitializedObserver
{
   
   public static boolean observered;
   
   public void observeInitialized(@Observes @BeforeBeanDiscovery BeanManager beanManager)
   {
      observered = true;
   }
   
}
