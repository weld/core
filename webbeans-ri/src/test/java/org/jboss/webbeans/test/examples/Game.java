package org.jboss.webbeans.test.examples;

import javax.webbeans.Initializer;

public class Game
{
   private final int number;
   
   @Initializer
   Game(@Random int number)
   {
      this.number = number;
   }

   public int getNumber()
   {
      return number;
   }
   
}
