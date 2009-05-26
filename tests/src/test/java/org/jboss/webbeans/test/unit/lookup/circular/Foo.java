package org.jboss.webbeans.test.unit.lookup.circular;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Current;

class Foo
{
   
   public static boolean success;
   
   @Current Bar bar;
   
   public Foo()
   {
      success = false;
   }
   
   @PostConstruct
   public void postConstruct()
   {
      if (bar.getName().equals("bar"))
      {
         success = true;
      }
   }
   
   public String getName()
   {
      return "foo";
   }
   
}
