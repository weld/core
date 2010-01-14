package org.jboss.weld.tests.ejb;

import javax.ejb.Stateful;

@Stateful
public class Cow
{
   
   private boolean pinged;
   
   public boolean isPinged()
   {
      return pinged;
   }
   
   public void ping()
   {
      pinged = true;
   }

}
