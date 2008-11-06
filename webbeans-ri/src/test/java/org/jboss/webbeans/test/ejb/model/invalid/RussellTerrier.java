package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Produces;

@Stateful
public class RussellTerrier
{
   @Remove
   @Produces
   public void destroy() {
      
   }
}
