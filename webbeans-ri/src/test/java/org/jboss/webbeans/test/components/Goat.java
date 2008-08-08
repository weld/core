package org.jboss.webbeans.test.components;

import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.annotations.Synchronous;

@Production
public class Goat
{
   
   public Goat(@Synchronous String foo)
   {
      // TODO Auto-generated constructor stub
   }
   
   public Goat(@Asynchronous String foo, String bar)
   {
      // TODO Auto-generated constructor stub
   }
   

}
