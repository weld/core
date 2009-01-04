package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.webbeans.Current;
import javax.webbeans.Initializer;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.test.contexts.valid.City;

@SessionScoped
public class Mikkeli extends City implements Serializable
{
   public Mikkeli()
   {
   }

   @Initializer
   public Mikkeli(@Current Violation reference)
   {
   }
   
}
