package org.jboss.webbeans.test.beans;

import javax.webbeans.Initializer;
import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Synchronous;

@Production
public class Goose
{
   
   public Goose(@Synchronous String foo)
   {
      // TODO Auto-generated constructor stub
   }
   
   @Initializer
   public Goose(String foo, String bar)
   {
      // TODO Auto-generated constructor stub
   }
   

}
