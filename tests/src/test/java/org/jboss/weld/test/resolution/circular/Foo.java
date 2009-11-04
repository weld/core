package org.jboss.weld.test.resolution.circular;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

class Foo
{
   
   public static boolean success;
   
   @Inject Bar bar;
   
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
