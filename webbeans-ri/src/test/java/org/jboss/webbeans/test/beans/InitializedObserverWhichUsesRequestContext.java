package org.jboss.webbeans.test.beans;

import javax.webbeans.Current;
import javax.webbeans.Observes;
import javax.webbeans.manager.Initialized;
import javax.webbeans.manager.Manager;

public class InitializedObserverWhichUsesRequestContext
{
   
   @Current Tuna tuna;
   
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
      tuna.getName();
   }
   
}
