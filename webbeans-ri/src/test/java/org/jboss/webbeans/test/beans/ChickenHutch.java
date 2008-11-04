package org.jboss.webbeans.test.beans;

import javax.webbeans.Initializer;

public class ChickenHutch
{
   
   public Fox fox;
   public Chicken chicken;
   
   @Initializer
   public void setFox(Fox fox)
   {
      this.fox = fox;
   }
   
   @Initializer
   public void setChicken(Chicken chicken)
   {
      this.chicken = chicken;
   }
   
}
