/**
 * 
 */
package org.jboss.weld.manager;

import javax.enterprise.context.spi.Context;

class CurrentActivity
{

   private final Context context;
   private final BeanManagerImpl manager;
	
   public CurrentActivity(Context context, BeanManagerImpl manager)
   {
      this.context = context;
      this.manager = manager;
   }

   public Context getContext()
   {
      return context;
   }
   
   public BeanManagerImpl getManager()
   {
      return manager;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof CurrentActivity)
      {
         return this.getContext().equals(((CurrentActivity) obj).getContext());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return getContext().hashCode();
   }
   
   @Override
   public String toString()
   {
      return getContext() + " -> " + getManager();
   }
}