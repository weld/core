package org.jboss.weld.tests.resolution.named;


public class NamedBeanConsumer
{
   
   /*@Inject @Named*/ FooBean foo;
   
   public FooBean getFoo()
   {
      return foo;
   }

}
