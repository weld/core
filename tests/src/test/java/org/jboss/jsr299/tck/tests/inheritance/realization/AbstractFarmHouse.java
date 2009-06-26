package org.jboss.jsr299.tck.tests.inheritance.realization;

import javax.enterprise.event.Observes;

abstract class AbstractFarmHouse
{
   
   public void observeTameCows(@Observes @Tame Cow cow)
   {
      
   }
   
}
