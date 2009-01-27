package org.jboss.webbeans.test.unit.lookup.circular;

import javax.inject.Initializer;

class Planet
{
   
   private Water water;
   
   @Initializer
   public Planet(Water water)
   {
      this.water = water;
   }
   
}
