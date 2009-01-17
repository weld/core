package org.jboss.webbeans.test.unit.bootstrap;

import javax.webbeans.Current;
import javax.webbeans.Observes;
import javax.webbeans.manager.Initialized;
import javax.webbeans.manager.Manager;

class InitializedObserverWhichUsesApplicationContext
{
   
   @Current LadybirdSpider spider;
   
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
      spider.spinWeb();
   }
   
}
