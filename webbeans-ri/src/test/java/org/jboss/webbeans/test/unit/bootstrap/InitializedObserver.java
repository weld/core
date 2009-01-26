package org.jboss.webbeans.test.unit.bootstrap;

import javax.event.Observes;
import javax.inject.manager.Initialized;
import javax.inject.manager.Manager;

class InitializedObserver
{
   
   public static boolean observered;
   
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
      observered = true;
   }
   
}
