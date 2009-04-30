package org.jboss.webbeans.test.unit.xml.parser.schema.foo;

import javax.inject.Initializer;

public class Order
{
   private int val;

   private String[] strArr;

   @Initializer
   public Order()
   {
      this.val = 0;
   }

   public Order(int val)
   {
      this.val = val;
   }

   public int getVal()
   {
      return this.val;
   }

   public String[] getStrArr()
   {
      return this.strArr;
   }
}
