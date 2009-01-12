package org.jboss.webbeans.test.contexts.invalid;

import javax.webbeans.Current;
import javax.webbeans.Produces;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.test.contexts.valid.City;

public class Peraseinajoki extends City
{

   @Produces @SessionScoped
   public Violation2 create(@Current Violation reference)
   {
      return new Violation2();
   }

}
