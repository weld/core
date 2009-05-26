package org.jboss.webbeans.test.unit.lookup.circular;

import javax.enterprise.inject.Initializer;

class Farm
{
   
   @Initializer
   public Farm(Farm farm)
   {
   }
   
}
