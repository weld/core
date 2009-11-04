package org.jboss.weld.tests.beanDeployment.mixed;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.inject.Named;

@Stateful
@Tame
@Named("Pongo")
class Hound implements HoundLocal
{ 
   @Remove
   public void bye() {
   }

}
