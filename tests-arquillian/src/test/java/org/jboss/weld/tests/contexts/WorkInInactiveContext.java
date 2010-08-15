package org.jboss.weld.tests.contexts;


public abstract class WorkInInactiveContext
{
   
   public void run()
   {
      boolean alreadyActive = false;
      try
      {
         alreadyActive = isContextActive();
         if (alreadyActive)
         {
            deactivateContext();
         }
         work();
      }
      finally
      {
         if (alreadyActive)
         {
            activateContext();
         }
      }
   }
   
   protected abstract void work();
   
   protected abstract boolean isContextActive();
   
   protected abstract void activateContext();
   
   protected abstract void deactivateContext();

}
