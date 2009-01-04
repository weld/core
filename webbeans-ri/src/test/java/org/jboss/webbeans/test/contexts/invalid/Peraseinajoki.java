package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.webbeans.Current;
import javax.webbeans.Produces;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.test.contexts.valid.City;

@SessionScoped
public class Peraseinajoki extends City implements Serializable
{

   public Peraseinajoki()
   {
   }

   @Produces @SessionScoped
   public Violation2 create(@Current Violation reference)
   {
      return new Violation2();
   }

}
