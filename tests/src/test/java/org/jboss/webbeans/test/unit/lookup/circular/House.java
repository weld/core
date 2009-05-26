package org.jboss.webbeans.test.unit.lookup.circular;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Initializer;

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
