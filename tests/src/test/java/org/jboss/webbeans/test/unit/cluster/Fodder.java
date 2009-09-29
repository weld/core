package org.jboss.webbeans.test.unit.cluster;

import java.io.Serializable;

public class Fodder implements Serializable
{

   private static final long serialVersionUID = 7526196594918652669L;
   
   private int amount;
   
   public int getAmount()
   {
      return amount;
   }
   
   public void setAmount(int amount)
   {
      this.amount = amount;
   }

}
