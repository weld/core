package org.jboss.webbeans.test.ejb.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Initializer;
import javax.webbeans.RequestScoped;

@Stateful
@RequestScoped
public class Saluki
{
   @Remove @Initializer
   public void bye() {
      
   }
   
}
