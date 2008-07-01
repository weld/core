package org.jboss.webbeans.test.components;

import javax.webbeans.Initializer;
import javax.webbeans.Production;

@Production
public class Sheep
{

   
   public Sheep(String foo)
   {
      
   }
   
   @Initializer
   public Sheep(String foo, Double bar)
   {
      
   }
   
}
