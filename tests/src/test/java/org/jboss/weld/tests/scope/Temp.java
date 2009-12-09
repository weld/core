package org.jboss.weld.tests.scope;

public class Temp
{
   
   private int number;
   
   public Temp(int number)
   {
      this.number = number;
   }
   
   public Temp()
   {
      number = 0;
   }
   
   public int getNumber()
   {
      return number;
   }
   
   public void setNumber(int number)
   {
      this.number = number;
   }

}
