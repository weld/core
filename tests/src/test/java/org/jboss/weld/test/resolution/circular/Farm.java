package org.jboss.weld.test.resolution.circular;

import javax.inject.Inject;

class Farm
{
   
   @Inject
   public Farm(Farm farm)
   {
   }
   
}
