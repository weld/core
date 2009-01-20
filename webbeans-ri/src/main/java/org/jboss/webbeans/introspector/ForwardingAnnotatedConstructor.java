package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

import org.jboss.webbeans.ManagerImpl;

public abstract class ForwardingAnnotatedConstructor<T> extends ForwardingAnnotatedMember<T, Constructor<T>> implements AnnotatedConstructor<T>
{

   @Override
   protected abstract AnnotatedConstructor<T> delegate();

   public List<AnnotatedParameter<?>> getAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedParameters(annotationType);
   }

   public AnnotatedType<T> getDeclaringClass()
   {
      return delegate().getDeclaringClass();
   }

   public List<AnnotatedParameter<?>> getParameters()
   {
      return delegate().getParameters();
   }

   public T newInstance(ManagerImpl manager)
   {
      return delegate().newInstance(manager);
   }

   public AnnotatedConstructor<T> wrap(Set<Annotation> annotations)
   {
      throw new UnsupportedOperationException();
   }
   
   
   
}
