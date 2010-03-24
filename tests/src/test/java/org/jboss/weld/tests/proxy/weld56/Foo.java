package org.jboss.weld.tests.proxy.weld56;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;


@RequestScoped
public class Foo
{

   @Inject
   public Foo(Bar bar)
   {
   }

   public String ping()
   {
      return "ping";
   }
}
