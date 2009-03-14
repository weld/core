package org.jboss.webbeans.test.unit.implementation.producer;

import javax.inject.Initializer;

public class IntInjection
{
   
   int value;
   
   @Initializer public IntInjection(Integer integer)
   {
      this.value = integer;
   }

}
