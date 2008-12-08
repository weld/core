package org.jboss.webbeans.test.ejb.invalid;

import javax.ejb.Stateless;
import javax.webbeans.Initializer;
import javax.webbeans.Observes;
import javax.webbeans.manager.Initialized;
import javax.webbeans.manager.Manager;

@Stateless
public class AustralianTerrier
{
   @Initializer
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
   }
}
