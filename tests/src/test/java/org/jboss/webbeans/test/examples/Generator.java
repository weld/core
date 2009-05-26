package org.jboss.webbeans.test.examples;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class Generator {
   
   private int lastInt = 0;
   private java.util.Random random = new java.util.Random( System.currentTimeMillis() ); 
   
   java.util.Random getRandom()
   {
      return random;
   }
   
   @Produces @Random int next() {
      int nextInt = getRandom().nextInt(100);
      while (nextInt == lastInt)
      {
         nextInt = getRandom().nextInt(100);
      }
      lastInt = nextInt;      
      return nextInt; 
   }

} 
