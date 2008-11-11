package org.jboss.webbeans.test.ejb.model.valid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Named;

import org.jboss.webbeans.test.annotations.Tame;

@Stateful
@Tame
@Named("Pongo")
public class Hound
{ 
   @Remove
   public void bye() {
   }

}
