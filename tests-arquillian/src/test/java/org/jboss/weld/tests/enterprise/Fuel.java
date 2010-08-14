package org.jboss.weld.tests.enterprise;

public class Fuel
{

   private final String type;

   Fuel(String type)
   {
      this.type = type;
   }
   
   public String getType()
   {
      return type;
   }
   
}
