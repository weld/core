package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.RequestScoped;

@Stateful
@RequestScoped
public class Poodle
{
   @Remove
   public void bye() {
      
   }
   
   @Remove
   public void byebye() {
      
   }
   
}
