package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Stateless;
import javax.webbeans.Destructor;
import javax.webbeans.Observes;
import javax.webbeans.manager.Initialized;
import javax.webbeans.manager.Manager;

@Stateless
public class CairnsTerrier
{
   @Destructor
   public void observeInitialized(@Observes @Initialized Manager manager)
   {
   }

}
