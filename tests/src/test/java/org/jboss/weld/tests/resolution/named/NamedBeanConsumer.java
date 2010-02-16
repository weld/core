package org.jboss.weld.tests.resolution.named;

import javax.inject.Inject;
import javax.inject.Named;


public class NamedBeanConsumer
{
   
   @Inject @Named FooBean foo;
   
   public FooBean getFoo()
   {
      return foo;
   }

}
