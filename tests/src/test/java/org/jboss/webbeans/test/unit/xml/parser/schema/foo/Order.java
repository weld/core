package org.jboss.webbeans.test.unit.xml.parser.schema.foo;

import javax.enterprise.inject.Initializer;

public class Order
{
   private int val;

   private String[] strArr;

   @Initializer
   public Order()
   {
      this.val = 0;
   }

   public Order(int val, String[] strArr)
   {
      this.val = val;
      this.strArr = strArr;
   }

   public int getVal()
   {
      return this.val;
   }

   public String[] getStrArr()
   {
      return this.strArr;
   }
   
   public void setStrArr(String[] strArr)
   {
      this.strArr = strArr;
   }
}
