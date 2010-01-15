package org.jboss.weld.tests.injectionPoint;

public class Cow
{
   
   private final String name;

   public Cow(String name)
   {
      this.name = name;
   }
   
   public String getName()
   {
      return name;
   }
   
}
