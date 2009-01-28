package org.jboss.webbeans.test.unit.lookup.circular;

import javax.context.ApplicationScoped;
import javax.inject.Initializer;

@ApplicationScoped
class House
{
   
   public House()
   {
      // TODO Auto-generated constructor stub
   }
   
   @Initializer
   public House(House house)
   {
      house.ping();
   }
   
   private void ping()
   {
      
   }
   
}
