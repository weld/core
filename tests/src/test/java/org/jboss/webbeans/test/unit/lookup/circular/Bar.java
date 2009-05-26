package org.jboss.webbeans.test.unit.lookup.circular;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Current;

@ApplicationScoped
class Bar
{
   
   public static boolean success;
   
   @Current Foo foo;
   
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
