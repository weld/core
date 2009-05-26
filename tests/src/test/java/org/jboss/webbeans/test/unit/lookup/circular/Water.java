package org.jboss.webbeans.test.unit.lookup.circular;

import javax.enterprise.inject.Initializer;

class Water
{
   @Initializer
   public Water(Fish fish)
   {
   }
   
}
