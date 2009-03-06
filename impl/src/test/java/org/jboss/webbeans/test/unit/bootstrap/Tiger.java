package org.jboss.webbeans.test.unit.bootstrap;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.inject.Production;

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
