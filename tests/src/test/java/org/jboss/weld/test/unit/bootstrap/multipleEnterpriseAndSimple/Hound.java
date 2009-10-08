package org.jboss.weld.test.unit.bootstrap.multipleEnterpriseAndSimple;

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
