package org.jboss.webbeans.test.ejb.model.valid;

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
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
   }

   public static void staticallyObserveInitialized(@Observes @Initialized Manager manager)
   {
   }
}
