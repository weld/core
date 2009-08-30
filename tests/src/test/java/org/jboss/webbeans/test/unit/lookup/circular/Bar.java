package org.jboss.webbeans.test.unit.lookup.circular;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
class Bar
{
   
   public static boolean success;
   
   @Inject Foo foo;
   
   public Bar()
   {
      success = false;
   }
   
   @PostConstruct
   public void postConstruct()
   {
      if (foo.getName().equals("foo"))
      {
         success = true;
      }
   }
   
   public String getName()
   {
      return "bar";
   }
   
}
