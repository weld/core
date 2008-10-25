package org.jboss.webbeans.test.beans;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Production;

@Production
@Stateful
public class Panther
{
   
   @Remove
   public void remove(String foo)
   {
      
   }

}
