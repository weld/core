package org.jboss.webbeans.test.unit.bootstrap;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Production;

@Production
@Stateful
@Synchronous
public class Tiger
{
   
   @Remove
   public void remove()
   {
      
   }

}
