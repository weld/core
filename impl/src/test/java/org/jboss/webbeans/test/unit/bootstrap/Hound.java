package org.jboss.webbeans.test.unit.bootstrap;

import javax.annotation.Named;
import javax.ejb.Remove;
import javax.ejb.Stateful;

@Stateful
@Tame
@Named("Pongo")
class Hound
{ 
   @Remove
   public void bye() {
   }

}
