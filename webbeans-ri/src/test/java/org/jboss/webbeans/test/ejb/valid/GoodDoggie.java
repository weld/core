package org.jboss.webbeans.test.ejb.valid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Destructor;

@Stateful
public class GoodDoggie
{
   @Destructor @Remove
   public void bye() {
   }

}
