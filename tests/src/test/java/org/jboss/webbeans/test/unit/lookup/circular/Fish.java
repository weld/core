package org.jboss.webbeans.test.unit.lookup.circular;

import javax.inject.Inject;

class Fish
{
   
   private Water water;
   
   @Inject
   public Fish(Water water)
   {
      this.water = water;
   }
   
}
