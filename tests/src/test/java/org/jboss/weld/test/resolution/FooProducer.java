package org.jboss.weld.test.resolution;

import javax.enterprise.inject.Produces;

public class FooProducer
{
   
   @Produces @Special
   public FooBase<Baz> produce()
   {
      return new FooBase<Baz>();
   }

}
