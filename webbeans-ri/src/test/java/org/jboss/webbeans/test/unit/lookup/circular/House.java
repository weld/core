package org.jboss.webbeans.test.unit.lookup.circular;

import javax.context.ApplicationScoped;
import javax.inject.Initializer;

@ApplicationScoped
class House
{
   
   // For serialization
   public House() {}
   
   @Initializer
   public House(House house)
   {
      house.ping();
   }
   
   private void ping()
   {
      
   }
   
}
