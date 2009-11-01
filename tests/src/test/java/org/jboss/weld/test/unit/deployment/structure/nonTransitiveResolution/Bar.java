package org.jboss.weld.test.unit.deployment.structure.nonTransitiveResolution;

import javax.inject.Inject;

public class Bar
{
   
   @Inject Foo foo;
   
   public Foo getFoo()
   {
      return foo;
   }

}
