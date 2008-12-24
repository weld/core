package org.jboss.webbeans.examples.numberguess;


import javax.webbeans.ApplicationScoped;
import javax.webbeans.Produces;

@ApplicationScoped
public class Generator {
   
   private java.util.Random random = new java.util.Random( System.currentTimeMillis() );
   
   private int maxNumber = 100;
   
   java.util.Random getRandom()
   {
      return random;
   }
   
   @Produces @Random int next() { 
      return getRandom().nextInt(maxNumber); 
   }
   
   @Produces @MaxNumber int getMaxNumber()
   {
      return maxNumber;
   }

} 
