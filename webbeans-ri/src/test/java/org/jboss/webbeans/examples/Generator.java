package org.jboss.webbeans.examples;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.Produces;

@ApplicationScoped
public class Generator {
   
   private java.util.Random random = new java.util.Random( System.currentTimeMillis() ); 
   
   @Produces @Random int next() { 
      return random.nextInt(100); 
   }
   
} 
