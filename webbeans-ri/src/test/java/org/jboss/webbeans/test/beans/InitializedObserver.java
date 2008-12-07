package org.jboss.webbeans.test.beans;

import javax.webbeans.Observes;
import javax.webbeans.manager.Initialized;
import javax.webbeans.manager.Manager;

public class InitializedObserver
{
   
   public static boolean observered;
   
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
      observered = true;
   }
   
}
