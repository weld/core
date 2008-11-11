package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Stateful;
import javax.webbeans.Destructor;
import javax.webbeans.RequestScoped;

@Stateful
@RequestScoped
public class Rottweiler
{
   @Destructor
   public void bye() {
      
   }
   
   @Destructor
   public void bye2() {
      
   }

}
