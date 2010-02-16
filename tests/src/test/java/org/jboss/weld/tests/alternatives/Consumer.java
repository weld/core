package org.jboss.weld.tests.alternatives;

import javax.inject.Inject;

public class Consumer
{

   @Inject Foo foo;
   
   public Foo getFoo()
   {
      return foo;
   }
   
}
