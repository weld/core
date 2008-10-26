package org.jboss.webbeans.test.beans;

import javax.webbeans.Initializer;
import javax.webbeans.Production;

@Production
public class Turkey
{
   
   public Turkey()
   {
      
   }
   
   @Initializer
   public Turkey(String foo, Integer bar)
   {
      
   }

}
