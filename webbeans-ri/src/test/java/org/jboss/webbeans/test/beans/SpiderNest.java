package org.jboss.webbeans.test.beans;

import javax.webbeans.Initializer;

public class SpiderNest
{
   
   public Integer numberOfSpiders;
   
   @Initializer
   public SpiderNest(Integer numberOfSpiders)
   {
      this.numberOfSpiders = numberOfSpiders; 
   }
   
}
