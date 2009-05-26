package org.jboss.webbeans.test.unit.bootstrap.multipleEnterprise;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.inject.deployment.Production;

@Production
@Stateful
class Panther implements PantherLocal
{
   
   @Remove
   public void remove(String foo)
   {
      
   }

}
