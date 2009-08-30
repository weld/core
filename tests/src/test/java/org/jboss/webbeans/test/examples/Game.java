package org.jboss.webbeans.test.examples;

import javax.inject.Inject;

public class Game
{
   private final int number;
   
   @Inject
   Game(@Random int number)
   {
      this.number = number;
   }

   public int getNumber()
   {
      return number;
   }
   
}
