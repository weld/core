package org.jboss.webbeans.test.unit.bootstrap;

import javax.event.Observes;
import javax.inject.Current;
import javax.inject.manager.Initialized;
import javax.inject.manager.Manager;

class InitializedObserverWhichUsesApplicationContext
{
   
   @Current LadybirdSpider spider;
   
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
      spider.spinWeb();
   }
   
}
