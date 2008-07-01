package org.jboss.webbeans.test.components;

import javax.webbeans.Initializer;
import javax.webbeans.Production;

@Production
public class Chicken
{
   
   @Initializer
   public Chicken(String foo)
   {
   }
   
   @Initializer
   public Chicken(String foo, Double bar)
   {
      
   }

}
