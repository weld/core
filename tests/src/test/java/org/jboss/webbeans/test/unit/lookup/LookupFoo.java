package org.jboss.webbeans.test.unit.lookup;

import javax.inject.Inject;

public class LookupFoo
{

   @Inject Foo foo;
   
   @Inject @Special FooBase<Baz> foobaz;
   
   public Foo getFoo()
   {
      return foo;
   }
   
   public FooBase<Baz> getFoobaz()
   {
      return foobaz;
   }
   
}
