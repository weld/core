package org.jboss.webbeans.test.ejb.valid;

import javax.ejb.Stateful;
import javax.webbeans.Named;
import javax.webbeans.Observes;
import javax.webbeans.manager.Initialized;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.test.annotations.Tame;

@Stateful
@Tame
@Named("Teddy")
public class Pomeranian
{
   public static Thread notificationThread;
   
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
   }

   public void observeSimpleEvent(@Observes String someEvent)
   {
      notificationThread = Thread.currentThread();
   }

   public static void staticallyObserveInitialized(@Observes @Initialized Manager manager)
   {
   }
}
