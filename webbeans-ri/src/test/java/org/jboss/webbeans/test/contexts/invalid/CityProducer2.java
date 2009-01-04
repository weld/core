package org.jboss.webbeans.test.contexts.invalid;

import javax.webbeans.Dependent;
import javax.webbeans.Produces;

public class CityProducer2
{
   @Produces
   @Dependent
   public Violation create()
   {
      return new Violation();
   }
}
