package org.jboss.webbeans.test.components;

import javax.ejb.Remove;
import javax.webbeans.Destroys;
import javax.webbeans.Production;

@Production
public class Cheetah
{
   
   @Remove @Destroys
   public void remove()
   {
      
   }
    

}
