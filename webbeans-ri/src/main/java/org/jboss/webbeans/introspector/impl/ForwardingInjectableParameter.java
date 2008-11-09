package org.jboss.webbeans.introspector.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.ModelManager;

public abstract class ForwardingInjectableParameter<T> extends InjectableParameter<T>
{
   
   protected abstract InjectableParameter<? extends T> delegate();
   
   @Override
   public Set<Annotation> getBindingTypes()
   {
      return delegate().getBindingTypes();
   }

   @Override
   public Set<Bean<?>> getMatchingBeans(List<Bean<?>> possibleBeans, ModelManager modelManager)
   {
      return delegate().getMatchingBeans(possibleBeans, modelManager);
   }

   @Override
   public Class<? extends T> getType()
   {
      return delegate().getType();
   }

   @Override
   public T getValue(ManagerImpl manager)
   {
      return delegate().getValue(manager);
   }

}
