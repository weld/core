package org.jboss.weld.tests.serialization;

import javax.enterprise.inject.Produces;

public class FooProducer
{

   @Produces
   public Foo produceFoo()
   {
      return new Foo("foo");
   }
   
}
