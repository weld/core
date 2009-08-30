package org.jboss.webbeans.test.unit.lookup.circular;

import javax.inject.Inject;

class Water
{
   @Inject
   public Water(Fish fish)
   {
   }
   
}
