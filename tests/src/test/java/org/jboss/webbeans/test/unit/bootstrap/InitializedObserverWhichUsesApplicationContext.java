package org.jboss.webbeans.test.unit.bootstrap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Current;
import javax.enterprise.inject.spi.AfterBeanDiscovery;

class InitializedObserverWhichUsesApplicationContext
{
   
   @Current Cow cow;
   
   public void observeInitialized(@Observes AfterBeanDiscovery event)
   {
      cow.moo();
   }
   
}
