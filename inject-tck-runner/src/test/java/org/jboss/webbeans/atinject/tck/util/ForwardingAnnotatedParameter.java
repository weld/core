package org.jboss.webbeans.atinject.tck.util;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;

public abstract class ForwardingAnnotatedParameter<X> extends ForwardingAnnotated implements AnnotatedParameter<X>
{

   @Override
   protected abstract AnnotatedParameter<X> delegate();

   public AnnotatedCallable<X> getDeclaringCallable()
   {
      return delegate().getDeclaringCallable();
   }

   public int getPosition()
   {
      return delegate().getPosition();
   }

}
