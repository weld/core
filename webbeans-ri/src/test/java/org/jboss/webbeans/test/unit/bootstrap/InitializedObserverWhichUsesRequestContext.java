package org.jboss.webbeans.test.unit.bootstrap;

import javax.webbeans.Current;
import javax.webbeans.Observes;
import javax.webbeans.manager.Initialized;
import javax.webbeans.manager.Manager;

class InitializedObserverWhichUsesRequestContext
{
   
   @Current Tuna tuna;
   
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
      tuna.getName();
   }
   
}
