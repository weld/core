package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Current;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.test.contexts.valid.City;

@Stateful
@SessionScoped
public class Pietarsaari extends City implements Serializable
{
   @Current
   private Violation reference;
   
   @Remove
   public void bye() {
      
   }
   
}
