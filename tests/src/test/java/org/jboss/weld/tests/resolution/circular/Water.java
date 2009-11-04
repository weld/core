package org.jboss.weld.tests.resolution.circular;

import javax.inject.Inject;

class Water
{
   @Inject
   public Water(Fish fish)
   {
   }
   
}
