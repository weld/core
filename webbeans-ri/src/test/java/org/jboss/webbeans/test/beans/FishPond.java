package org.jboss.webbeans.test.beans;

import javax.webbeans.Initializer;

public class FishPond
{
   
   public Animal goldfish;
   
   @Initializer
   public FishPond(Goldfish goldfish)
   {
      this.goldfish = goldfish;
   }
   
   
}
