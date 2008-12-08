package org.jboss.webbeans.test.ejb.invalid;

import javax.ejb.Stateless;
import javax.webbeans.Disposes;
import javax.webbeans.Observes;
import javax.webbeans.manager.Initialized;
import javax.webbeans.manager.Manager;

@Stateless
public class FoxTerrier
{
   public void observeInitialized(@Observes @Initialized Manager manager, @Disposes String badParam)
   {
   }

}
