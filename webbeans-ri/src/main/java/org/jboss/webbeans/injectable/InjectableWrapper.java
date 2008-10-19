package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Manager;

public class InjectableWrapper<T, S> extends Injectable<T, S>
{

   private Injectable<T, S> delegate;

   public InjectableWrapper(Injectable<T, S> delegate)
   {
      this.delegate = delegate;
   }

   @Override
   public Annotation[] getBindingTypes()
   {
      return delegate.getBindingTypes();
   }

   @Override
   public Set<Bean<?>> getPossibleTargets(Set<Bean<?>> possibleBeans)
   {
      return delegate.getPossibleTargets(possibleBeans);
   }

   @Override
   public Class<? extends T> getType()
   {
      return delegate.getType();
   }

   @Override
   public T getValue(Manager manager)
   {
      return delegate.getValue(manager);
   }

}