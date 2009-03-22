package org.jboss.webbeans.test.unit.bootstrap.ordering;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.inject.Production;

@Production
@Stateful
class Gazelle implements GazelleLocal
{
   
   @Remove
   public void remove1()
   {
      
   }

}
