package org.jboss.webbeans.test.ejb.valid;

import javax.ejb.Stateful;
import javax.webbeans.Named;
import javax.webbeans.Observes;

import org.jboss.webbeans.test.annotations.Tame;

@Stateful
@Tame
@Named("Teddy")
public class Pomeranian implements PomeranianInterface
{
   public static Thread notificationThread;
   
   public void observeSimpleEvent(@Observes String someEvent)
   {
      notificationThread = Thread.currentThread();
   }

   public static void staticallyObserveEvent(@Observes String someEvent)
   {
   }
}
