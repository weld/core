package org.jboss.webbeans.test.components;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Destroys;
import javax.webbeans.Production;

@Production
@Stateful
public class Cougar
{
   
   @Remove
   public void remove()
   {
      
   }
   
   @Remove @Destroys
   public void remove1()
   {
      
   }

   @Remove @Destroys
   public void remove2()
   {
      
   }
   
}
