package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Stateless;
import javax.webbeans.Destructor;

@Stateless
public class Armant
{
   @Destructor
   public void bye() {
      
   }
}
