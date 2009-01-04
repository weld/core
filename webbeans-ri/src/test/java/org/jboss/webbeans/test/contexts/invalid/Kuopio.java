package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.webbeans.Current;
import javax.webbeans.Initializer;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.test.contexts.valid.City;

@SessionScoped
public class Kuopio extends City implements Serializable
{
   public Kuopio() {
      
   }
   
   @Initializer
   public void init(@Current Violation reference) {
      
   }
}
