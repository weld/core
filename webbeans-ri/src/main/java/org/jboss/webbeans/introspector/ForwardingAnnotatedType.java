package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;



public abstract class ForwardingAnnotatedType<T> extends ForwardingAnnotatedItem<T, Class<T>> implements AnnotatedType<T>
{

   @Override
   protected abstract AnnotatedType<T> delegate();

   public AnnotatedType<?> getSuperclass()
   {
      return delegate().getSuperclass();
   }

   public boolean isEquivalent(Class<?> clazz)
   {
      return delegate().isEquivalent(clazz);
   }
   
   @Override
   public AnnotatedType<T> wrap(Set<Annotation> annotations)
   {
      throw new UnsupportedOperationException();
   }
   
}
