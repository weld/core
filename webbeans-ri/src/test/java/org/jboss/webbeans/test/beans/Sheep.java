package org.jboss.webbeans.test.beans;

import javax.webbeans.Initializer;
import javax.webbeans.Production;

@Production
public class Sheep
{
   
   @Initializer
   public Sheep(String foo, Double bar)
   {
      
   }
   
}
