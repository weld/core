package org.jboss.webbeans.contexts;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.Dependent;
import javax.webbeans.manager.Bean;

public class DependentContext extends AbstractContext
{
   private BeanMap beans;
   private AtomicBoolean active;
   
   public DependentContext()
   {
      super(Dependent.class);
      beans = new BeanMap();
      active = new AtomicBoolean(false);
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
   protected AtomicBoolean getActive()
   {
      return active;
   }

   @Override
   protected BeanMap getBeanMap()
   {
      return beans;
   }
   
   @Override
   public String toString()
   {
      return "Dependent context";
   }   
}
