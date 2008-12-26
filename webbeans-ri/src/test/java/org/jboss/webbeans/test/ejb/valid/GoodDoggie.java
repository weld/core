package org.jboss.webbeans.test.ejb.valid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Destructor;

import org.jboss.webbeans.test.ejb.EnterpriseBeanRemoveMethodTest;

@Stateful
public class GoodDoggie implements LocalGoodDoggie
{
   @Destructor @Remove
   public void bye() {
      EnterpriseBeanRemoveMethodTest.visited = true;
   }

}
