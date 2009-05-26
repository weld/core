package org.jboss.webbeans.test.unit.implementation.producer.method;

import javax.enterprise.inject.Initializer;

public class IntInjection
{
   
   int value;
   
   @Initializer public IntInjection(Integer integer)
   {
      this.value = integer;
   }

}
