package org.jboss.webbeans.test.unit.lookup;

import javax.enterprise.inject.Current;

public class LookupFoo
{

   @Current Foo foo;
   
   @Special FooBase<Baz> foobaz;
   
   public Foo getFoo()
   {
      return foo;
   }
   
   public FooBase<Baz> getFoobaz()
   {
      return foobaz;
   }
   
}
