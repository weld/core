package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.ModelManager;

public class InjectableParameterWrapper<T> extends InjectableParameter<T>
{
   
   private InjectableParameter<? extends T> delegate;

   public InjectableParameterWrapper(InjectableParameter<? extends T> delegate)
   {
      this.delegate = delegate;
   }
   
   @Override
   public Set<Annotation> getBindingTypes()
   {
      return delegate.getBindingTypes();
   }

   @Override
   public Set<Bean<?>> getMatchingBeans(Set<Bean<?>> possibleBeans, ModelManager modelManager)
   {
      return delegate.getMatchingBeans(possibleBeans, modelManager);
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
