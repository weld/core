package org.jboss.weld.tests.extensions.annotatedType.invalidParameters;

import javax.inject.Inject;



public class TumbleDryer
{

   private final Clothes clothers;
   
   @Inject
   public TumbleDryer(Clothes clothes)
   {
      this.clothers = clothes;
   }
   
   public Clothes getClothes()
   {
      return clothers;
   }
   
}
