package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.webbeans.Current;
import javax.webbeans.Initializer;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.test.contexts.valid.City;

@SessionScoped
public class Loviisa extends City implements Serializable
{
   public Loviisa() {
   }
   
   @Initializer
   public Loviisa(@Current Violation reference) {
      
   }
}
