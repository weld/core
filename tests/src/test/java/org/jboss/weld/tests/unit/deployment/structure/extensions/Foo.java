package org.jboss.weld.tests.unit.deployment.structure.extensions;

import javax.enterprise.inject.Produces;

public class Foo
{

   @Produces
   public String get()
   {
      return "Foo!";
   }
   
}
