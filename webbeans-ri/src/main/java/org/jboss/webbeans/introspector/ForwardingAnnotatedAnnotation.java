package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;

public abstract class ForwardingAnnotatedAnnotation<T extends Annotation> extends ForwardingAnnotatedType<T> implements AnnotatedAnnotation<T>
{
   
   @Override
   protected abstract AnnotatedAnnotation<T> delegate();
   
   public Set<AnnotatedMethod<?>> getAnnotatedMembers(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedMembers(annotationType);
   }
   
   public Set<AnnotatedMethod<?>> getMembers()
   {
      return delegate().getMembers();
   }
   
   public AnnotatedAnnotation<T> wrap(Set<Annotation> annotations)
   {
      throw new UnsupportedOperationException();
   }
   
}
