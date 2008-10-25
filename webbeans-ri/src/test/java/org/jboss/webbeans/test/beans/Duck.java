package org.jboss.webbeans.test.beans;

import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Synchronous;

@Production
public class Duck
{

   public Duck(String foo)
   {
      // TODO Auto-generated constructor stub
   }
   
   public Duck(String foo, @Synchronous Integer bar)
   {
      
   }
   
}
