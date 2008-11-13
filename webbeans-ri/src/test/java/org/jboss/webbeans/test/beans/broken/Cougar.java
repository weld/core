package org.jboss.webbeans.test.beans.broken;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Destructor;
import javax.webbeans.Production;

@Production
@Stateful
public class Cougar
{
   
   @Remove
   public void remove()
   {
      
   }
   
   @Remove @Destructor
   public void remove1()
   {
      
   }

   @Remove @Destructor
   public void remove2()
   {
      
   }
   
}
