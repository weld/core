package org.jboss.weld.atinject.tck.util;

import java.lang.reflect.Member;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;

public abstract class ForwardingAnnotatedMember<X> extends ForwardingAnnotated implements AnnotatedMember<X>
{

   @Override
   protected abstract AnnotatedMember<X> delegate();

   public AnnotatedType<X> getDeclaringType()
   {
      return delegate().getDeclaringType();
   }

   public Member getJavaMember()
   {
      return delegate().getJavaMember();
   }

   public boolean isStatic()
   {
      return delegate().isStatic();
   }

}
