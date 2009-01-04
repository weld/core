package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.webbeans.Produces;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.test.contexts.valid.City;

@SessionScoped
public class Jamsa extends City implements Serializable
{
   public Jamsa()
   {
   }

   @Produces
   @SessionScoped
   public Violation create()
   {
      return new Violation();
   }
}
