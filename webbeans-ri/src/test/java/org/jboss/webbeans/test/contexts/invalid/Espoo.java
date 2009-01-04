package org.jboss.webbeans.test.contexts.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Current;
import javax.webbeans.SessionScoped;

@Stateful
@SessionScoped
public class Espoo
{
   @Current
   Violation reference;
   
   @Remove
   public void bye() {
   }
}
