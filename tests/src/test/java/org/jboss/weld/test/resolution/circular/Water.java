package org.jboss.weld.test.resolution.circular;

import javax.inject.Inject;

class Water
{
   @Inject
   public Water(Fish fish)
   {
   }
   
}
