package org.jboss.webbeans.test.unit.lookup.circular;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Initializer;

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
