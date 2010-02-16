package org.jboss.weld.tests.alternatives;

import javax.enterprise.inject.Produces;

public class Producer
{
   
   @Produces @Test 
   public Foo getFoo() 
   {
      return new Foo("Test");
   }
   
   @Produces
   public Foo getManager() 
   {
      return new Foo("Normal");
   }

}
