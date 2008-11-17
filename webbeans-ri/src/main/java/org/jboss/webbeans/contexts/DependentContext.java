package org.jboss.webbeans.contexts;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.Dependent;
import javax.webbeans.manager.Bean;

public class DependentContext extends PrivateContext
{

   public DependentContext()
   {
      super(Dependent.class);
      setActive(false);
   }

   @Override
   public <T> T get(Bean<T> bean, boolean create)
   {
      if (!isActive())
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
}
