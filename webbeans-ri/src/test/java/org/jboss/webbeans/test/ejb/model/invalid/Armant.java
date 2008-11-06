package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateful;

@Stateful
public class Armant
{
   @Remove
   public void destroy() {
      
   }
}
