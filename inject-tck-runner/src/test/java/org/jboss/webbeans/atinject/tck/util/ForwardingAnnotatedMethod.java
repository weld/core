package org.jboss.webbeans.atinject.tck.util;

import java.lang.reflect.Method;

import javax.enterprise.inject.spi.AnnotatedMethod;

public abstract class ForwardingAnnotatedMethod<X> extends ForwardingAnnotatedCallable<X> implements AnnotatedMethod<X>
{

   @Override
   protected abstract AnnotatedMethod<X> delegate();
   
   @Override
   public Method getJavaMember()
   {
      return delegate().getJavaMember();
   }

}
