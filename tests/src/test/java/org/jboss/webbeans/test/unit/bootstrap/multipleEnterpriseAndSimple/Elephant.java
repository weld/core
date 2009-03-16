package org.jboss.webbeans.test.unit.bootstrap.multipleEnterpriseAndSimple;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.inject.Production;

@Production
@Stateful
class Elephant implements ElephantLocal
{
   
   @Remove
   public void remove1()
   {
      
   }
   
   @Remove
   public void remove2()
   {
      
   }

}
