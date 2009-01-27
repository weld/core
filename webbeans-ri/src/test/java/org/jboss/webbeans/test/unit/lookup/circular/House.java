package org.jboss.webbeans.test.unit.lookup.circular;

import javax.context.ApplicationScoped;
import javax.inject.Initializer;

@ApplicationScoped
class House
{
   
   @Initializer
   public House(House farm)
   {
   }
   
}
