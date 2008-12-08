package org.jboss.webbeans.test.ejb.invalid;

import javax.ejb.Stateless;
import javax.webbeans.Observes;
import javax.webbeans.Produces;
import javax.webbeans.manager.Initialized;
import javax.webbeans.manager.Manager;

@Stateless
public class BorderTerrier
{
   @Produces
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
   }
}
