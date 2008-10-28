package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.Dependent;
import javax.webbeans.manager.Bean;

public class DependentContext extends PseudoContext
{

   public DependentContext(Class<? extends Annotation> scopeType)
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
}
