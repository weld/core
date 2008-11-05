package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Initializer;
import javax.webbeans.Production;

@Production
public class Goose
{
   
   @Initializer
   public Goose(String foo)
   {
   }
   
   @Initializer
   public Goose(String foo, Double bar)
   {
      
   }

}
