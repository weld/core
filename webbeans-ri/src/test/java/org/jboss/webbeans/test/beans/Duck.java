package org.jboss.webbeans.test.beans;

import javax.webbeans.Initializer;
import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Synchronous;

@Production
public class Duck
{
   
   @Initializer
   public Duck(String foo, @Synchronous Integer bar)
   {
      
   }
   
}
