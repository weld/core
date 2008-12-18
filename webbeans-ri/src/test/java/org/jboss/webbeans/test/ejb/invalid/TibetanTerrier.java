package org.jboss.webbeans.test.ejb.invalid;

import javax.ejb.Stateless;
import javax.webbeans.Observes;

@Stateless
public class TibetanTerrier implements Terrier
{
   public void observeInitialized(@Observes String someEvent)
   {
   }
}
