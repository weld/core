package org.jboss.webbeans.test.unit.bootstrap;

import javax.enterprise.inject.Current;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.event.Observes;

class InitializedObserverWhichUsesApplicationContext
{
   
   @Current Cow cow;
   
   public void observeInitialized(@Observes AfterBeanDiscovery event)
   {
      cow.moo();
   }
   
}
