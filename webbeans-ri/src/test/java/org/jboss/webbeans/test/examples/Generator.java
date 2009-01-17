package org.jboss.webbeans.test.examples;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.Produces;

@ApplicationScoped
public class Generator {
   
   private java.util.Random random = new java.util.Random( System.currentTimeMillis() ); 
   
   java.util.Random getRandom()
   {
      return random;
   }
   
   @Produces @Random int next() { 
      return getRandom().nextInt(100); 
   }

} 
