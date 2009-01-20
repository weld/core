package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.manager.Manager;

public abstract class ForwardingAnnotatedParameter<T> extends ForwardingAnnotatedItem<T, Object> implements AnnotatedParameter<T>
{

   @Override
   protected abstract AnnotatedParameter<T> delegate();

   public AnnotatedMember<?, ?> getDeclaringMember()
   {
      return delegate().getDeclaringMember();
   }

   public T getValue(Manager manager)
   {
      return delegate().getValue(manager);
   }

   public AnnotatedParameter<T> wrap(Set<Annotation> annotations)
   {
      throw new UnsupportedOperationException();
   }
   
}
