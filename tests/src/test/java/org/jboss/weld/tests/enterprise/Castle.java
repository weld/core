package org.jboss.weld.tests.enterprise;

import javax.ejb.Stateless;

@Stateless
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
