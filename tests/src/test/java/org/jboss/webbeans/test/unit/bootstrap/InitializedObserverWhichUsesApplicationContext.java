package org.jboss.webbeans.test.unit.bootstrap;

import javax.enterprise.inject.Current;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.event.Observes;

class InitializedObserverWhichUsesApplicationContext
{
   
   @Current Cow cow;
   
   public void observeInitialized(@Observes @BeforeBeanDiscovery BeanManager beanManager)
   {
      cow.moo();
   }
   
}
