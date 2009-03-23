package org.jboss.webbeans.test.unit.bootstrap.ordering;

import javax.annotation.Named;
import javax.inject.Produces;


class Shop
{
   
   @Produces @Expensive @Named
   public Product getExpensiveGift()
   {
      return new Product();
   }
   
}
