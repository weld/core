package org.jboss.webbeans.test.contexts.valid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.SessionScoped;

@Stateful
@SessionScoped
public class Turku
{
   @Remove
   public void bye() {
      
   }
}
