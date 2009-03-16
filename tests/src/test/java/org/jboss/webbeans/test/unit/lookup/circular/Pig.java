package org.jboss.webbeans.test.unit.lookup.circular;

import javax.annotation.PostConstruct;
import javax.context.ApplicationScoped;
import javax.inject.Current;

@ApplicationScoped
class Pig
{
   
   public static boolean success;
   
   @Current Food food;
   
   public Pig()
   {
      success = false;
   }
   
   @PostConstruct
   public void postConstruct()
   {
      if (food.getName().equals("food"))
      {
         success = true;
      }
   }
   
   public String getName()
   {
      return "john";
   }
   
}
