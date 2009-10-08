package org.jboss.weld.test.unit.bootstrap.multipleEnterprise;

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
