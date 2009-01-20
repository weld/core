package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Set;

public abstract class ForwardingAnnotatedMember<T, S extends Member> extends ForwardingAnnotatedItem<T, S> implements AnnotatedMember<T, S>
{
   
   @Override
   protected abstract AnnotatedMember<T, S> delegate();
   
   public S getMember()
   {
      return delegate().getMember();
   }
   
   @Override
   public AnnotatedMember<T, S> wrap(Set<Annotation> annotations)
   {
      throw new UnsupportedOperationException();
   }
   
}
