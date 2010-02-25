package org.jboss.weld.tests.producer.method.parameterized;

import javax.enterprise.inject.Produces;

public class ParameterizedProducer
{

   @Produces
   @Parameterized
   public Parameterized1<Parameterized2<Double>> getParameterized()
   {
      return new Parameterized1();
   }
}
