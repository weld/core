package org.jboss.webbeans.test.unit.bootstrap;

import javax.webbeans.Observes;
import javax.webbeans.manager.Initialized;
import javax.webbeans.manager.Manager;

class InitializedObserver
{
   
   public static boolean observered;
   
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
      observered = true;
   }
   
}
