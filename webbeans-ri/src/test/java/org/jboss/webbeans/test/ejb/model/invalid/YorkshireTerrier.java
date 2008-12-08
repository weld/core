package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Stateless;
import javax.webbeans.Observes;
import javax.webbeans.manager.Initialized;
import javax.webbeans.manager.Manager;

@Stateless
public class YorkshireTerrier
{
   public void observeInitialized(@Observes @Initialized Manager manager, @Observes Boxer anotherDog)
   {
   }
}
