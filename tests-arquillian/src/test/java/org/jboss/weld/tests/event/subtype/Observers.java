package org.jboss.weld.tests.event.subtype;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class Observers
{

   private Foo foo;
   private Bar bar;
   
   void observeFoo(@Observes Foo foo)
   {
      this.foo = foo;
   }
   
   void observeBar(@Observes Bar bar)
   {
      this.bar = bar;
   }
   
   public Bar getBar()
   {
      return bar;
   }
   
   public Foo getFoo()
   {
      return foo;
   }
   
   public void reset()
   {
      this.foo = null;
      this.bar = null;
   }
   
}
