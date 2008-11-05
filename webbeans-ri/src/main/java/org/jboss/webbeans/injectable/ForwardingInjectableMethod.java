package org.jboss.webbeans.injectable;

import java.util.Set;

import javax.webbeans.manager.Manager;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedMethod;

public abstract class ForwardingInjectableMethod<T> extends InjectableMethod<T>
{
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }
   
   @Override
   public AnnotatedMethod<T> getAnnotatedItem()
   {
      return delegate().getAnnotatedItem();
   }
   
   @Override
   public Set<InjectableParameter<Object>> getParameters()
   {
      return delegate().getParameters();
   }
   
   @Override
   protected Object[] getParameterValues(ManagerImpl manager)
   {
      return delegate().getParameterValues(manager);
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   public T invoke(Manager manager, Object instance, Object[] parameters)
   {
      return delegate().invoke(manager, instance, parameters);
   }
   
   @Override
   public T invoke(ManagerImpl manager, Object instance)
   {
      return delegate().invoke(manager, instance);
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }
   
   protected abstract InjectableMethod<T> delegate();
   
}
