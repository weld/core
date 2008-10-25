package org.jboss.webbeans.test.beans;

import javax.ejb.Remove;
import javax.webbeans.Destructor;
import javax.webbeans.Production;

@Production
public class Cheetah
{
   
   @Remove @Destructor
   public void remove()
   {
      
   }
    

}
