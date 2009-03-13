package org.jboss.webbeans.test.unit.bootstrap;

import javax.event.Observes;
import javax.inject.Current;
import javax.inject.manager.Initialized;
import javax.inject.manager.Manager;

class InitializedObserverWhichUsesRequestContext
{
   
   public static String name;
   
   @Current Tuna tuna;
   
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
      name = tuna.getName();
   }
   
}
