package org.jboss.webbeans.test.unit.bootstrap;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Named;

@Stateful
@Tame
@Named("Pongo")
class Hound
{ 
   @Remove
   public void bye() {
   }

}
