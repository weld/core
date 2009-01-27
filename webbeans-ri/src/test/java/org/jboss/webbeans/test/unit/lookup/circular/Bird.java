package org.jboss.webbeans.test.unit.lookup.circular;

import javax.context.ApplicationScoped;
import javax.inject.Initializer;

@ApplicationScoped
class Bird
{
   
   private Water water;
   
   @Initializer
   public Bird(Water water)
   {
      this.water = water;
   }
   
}
