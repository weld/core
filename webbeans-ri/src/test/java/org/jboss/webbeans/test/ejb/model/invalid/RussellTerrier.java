package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Stateful;
import javax.webbeans.Destructor;
import javax.webbeans.RequestScoped;

@Stateful
@RequestScoped
public class RussellTerrier
{
   @Destructor
   public void bye() {
      
   }
   
}
