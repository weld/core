package org.jboss.webbeans.test.contexts.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.SessionScoped;

@Stateful
@SessionScoped
public class Porvoo implements CityInterface
{
   public void foo()
   {
   }

   @Remove
   public void bye()
   {
   }
}
