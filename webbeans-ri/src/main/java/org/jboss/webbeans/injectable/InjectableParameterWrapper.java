package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;

public class InjectableParameterWrapper<T> extends InjectableParameter<T>
{
   
   private InjectableParameter<T> delegate;

   public InjectableParameterWrapper(InjectableParameter<T> delegate)
   {
      this.delegate = delegate;
   }
   
   @Override
   public Set<Annotation> getBindingTypes()
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
   public T getValue(ManagerImpl manager)
   {
      return delegate.getValue(manager);
   }

}
