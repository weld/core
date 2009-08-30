package org.jboss.webbeans.test.unit.implementation.producer.method;

import javax.inject.Inject;

public class IntInjection
{
   
   int value;
   
   @Inject public IntInjection(Integer integer)
   {
      this.value = integer;
   }

}
