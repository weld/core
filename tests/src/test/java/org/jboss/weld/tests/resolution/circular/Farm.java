package org.jboss.weld.tests.resolution.circular;

import javax.inject.Inject;

class Farm
{
   
   @Inject
   public Farm(Farm farm)
   {
   }
   
}
