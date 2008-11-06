package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Manager;

public class BeanWrapper<T> extends Bean<T>
{

   private Bean<T> delegate;
   
   public BeanWrapper(Manager manager, Bean<T> delegate)
   {
      super(manager);
      this.delegate = delegate;
   }

   @Override
   public T create()
   {
      return delegate.create();
   }

   @Override
   public void destroy(T instance)
   {
      delegate.destroy(instance);
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      return delegate.getBindingTypes();
   }

   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
      return delegate.getDeploymentType();
   }

   @Override
   public String getName()
   {
      return delegate.getName();
   }

   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return delegate.getScopeType();
   }

   @Override
   public Set<Class<?>> getTypes()
   {
      return delegate.getTypes();
   }

   @Override
   public boolean isNullable()
   {
      return delegate.isNullable();
   }

   @Override
   public boolean isSerializable()
   {
      return delegate.isSerializable();
   }
   
   @Override
   public String toString()
   {
      return delegate.toString();
   }
   
   @Override
   public int hashCode()
   {
      return delegate.hashCode();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate.equals(obj);
   }
   
}
