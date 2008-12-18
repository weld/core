package org.jboss.webbeans.test.ejb.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateless;
import javax.webbeans.Destructor;

@Stateless
public class Armant
{
   @Destructor @Remove
   public void bye() {
      
   }
}
