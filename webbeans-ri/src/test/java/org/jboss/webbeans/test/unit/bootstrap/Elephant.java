package org.jboss.webbeans.test.unit.bootstrap;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Destructor;
import javax.webbeans.Production;

@Production
@Stateful
class Elephant
{
   
   @Remove
   public void remove1()
   {
      
   }
   
   @Remove @Destructor
   public void remove2()
   {
      
   }

}
