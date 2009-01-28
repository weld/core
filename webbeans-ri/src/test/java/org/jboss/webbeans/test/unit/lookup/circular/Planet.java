package org.jboss.webbeans.test.unit.lookup.circular;

import javax.inject.Initializer;

class Planet
{
   
   private Space space;
   
   @Initializer
   public Planet(Space space)
   {
      this.space = space;
   }
   
}
