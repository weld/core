package org.jboss.weld.tests.resolution;

import javax.enterprise.inject.Produces;

public class FooProducer
{
   
   @Produces @Special
   public FooBase<Baz> produce()
   {
      return new FooBase<Baz>();
   }

}
