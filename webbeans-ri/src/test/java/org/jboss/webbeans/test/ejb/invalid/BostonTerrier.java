package org.jboss.webbeans.test.ejb.invalid;

import java.util.List;

import javax.ejb.Stateless;
import javax.webbeans.Observes;

@Stateless
public class BostonTerrier
{
   public void observesBadEvent(@Observes List<String> someArray)
   {
   }
}
