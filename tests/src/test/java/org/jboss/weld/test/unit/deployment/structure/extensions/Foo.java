package org.jboss.weld.test.unit.deployment.structure.extensions;

import javax.enterprise.inject.Produces;

public class Foo
{

   @Produces
   public String get()
   {
      return "Foo!";
   }
   
}
