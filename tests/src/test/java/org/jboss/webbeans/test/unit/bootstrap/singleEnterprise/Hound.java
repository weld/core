package org.jboss.webbeans.test.unit.bootstrap.singleEnterprise;

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
