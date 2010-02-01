package org.jboss.weld.tests.enterprise;

import javax.ejb.Stateful;

@Stateful
public class Castle
{
   
   private boolean pinged;
   
   public boolean isPinged()
   {
      return pinged;
   }
   
   public void ping()
   {
      this.pinged = true;
   }

}
