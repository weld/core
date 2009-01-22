package org.jboss.webbeans.test.unit.bootstrap;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Production;

@Production
@Stateful
class Elephant
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
