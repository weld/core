package org.jboss.weld.tests.beanDeployment.mixed;

import javax.ejb.Remove;
import javax.ejb.Stateful;

@Stateful
class Panther implements PantherLocal
{
   
   @Remove
   public void remove(String foo)
   {
      
   }

}
