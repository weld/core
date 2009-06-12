package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Decorator;

public abstract class ForwardingDecorator<T> extends ForwardingBean<T> implements Decorator<T>
{

   @Override
   protected abstract Decorator<T> delegate();

   public Set<Type> getDecoratedTypes()
   {
      return delegate().getDecoratedTypes();
   }

   public Set<Annotation> getDelegateBindings()
   {
      return delegate().getDelegateBindings();
   }

   public Type getDelegateType()
   {
      return delegate().getDelegateType();
   }

}
