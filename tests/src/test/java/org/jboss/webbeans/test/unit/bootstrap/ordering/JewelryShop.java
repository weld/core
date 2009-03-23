package org.jboss.webbeans.test.unit.bootstrap.ordering;

import javax.inject.Produces;
import javax.inject.Specializes;

class JewelryShop extends Shop
{
   
   @Override @Produces @Specializes @Sparkly @AnotherDeploymentType
   public Product getExpensiveGift()
   {
      return new Necklace();
   }
   
}
