package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Initializer;

@Stateful
public class JackRussellTerrier
{
   @Remove
   @Initializer
   public void destroy() {
      
   }
}
