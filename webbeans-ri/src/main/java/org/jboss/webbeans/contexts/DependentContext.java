package org.jboss.webbeans.contexts;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.Dependent;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Manager;

public class DependentContext extends AbstractContext
{

   public DependentContext()
   {
      super(Dependent.class);
      active = false;
   }

   @Override
   public <T> T get(Bean<T> bean, boolean create)
   {
      if (!active)
      {
         throw new ContextNotActiveException();
      }

      return create == false ? null : bean.create();
   }   

   @Override
   public String toString()
   {
      return "Dependent context";
   }
   
   @Override
   public void destroy(Manager manager)
   {
      
   }
   
}
