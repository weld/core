package org.jboss.webbeans.introspector;

import java.lang.reflect.Member;

public abstract class ForwardingAnnotatedMember<T, S extends Member> extends ForwardingAnnotatedItem<T, S> implements AnnotatedMember<T, S>
{
   
   @Override
   protected abstract AnnotatedMember<T, S> delegate();
   
   public S getMember()
   {
      return delegate().getMember();
   }
   
}
