package org.jboss.webbeans.test.unit.bootstrap.multipleEnterprise;

import javax.annotation.Named;
import javax.ejb.Remove;
import javax.ejb.Stateful;

@Stateful
@Tame
@Named("Pongo")
class Hound implements HoundLocal
{ 
   @Remove
   public void bye() {
   }

}
