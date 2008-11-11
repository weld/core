package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Produces;
import javax.webbeans.RequestScoped;

@Stateful
@RequestScoped
public class Spitz
{
   @Remove @Produces
   public void bye() {
      
   }
   
}
