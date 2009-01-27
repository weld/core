package org.jboss.webbeans.test.unit.lookup.circular;

import javax.inject.Initializer;

class Water
{
   @Initializer
   public Water(Fish fish)
   {
   }
   
}
