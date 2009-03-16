package org.jboss.webbeans.test.unit.lookup.circular;

import javax.context.ApplicationScoped;
import javax.inject.Initializer;

@ApplicationScoped
class Bird
{
   
   private Air air;
   
   public Bird()
   {
      
   }
   
   @Initializer
   public Bird(Air air)
   {
      this.air = air;
   }
   
}
