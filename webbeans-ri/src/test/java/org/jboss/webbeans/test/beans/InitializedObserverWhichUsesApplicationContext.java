package org.jboss.webbeans.test.beans;

import javax.webbeans.Current;
import javax.webbeans.Observes;
import javax.webbeans.manager.Initialized;
import javax.webbeans.manager.Manager;

public class InitializedObserverWhichUsesApplicationContext
{
   
   @Current LadybirdSpider spider;
   
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
      spider.spinWeb();
   }
   
}
