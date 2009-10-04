package org.jboss.webbeans.atinject.tck.util;

import java.lang.reflect.Constructor;

import javax.enterprise.inject.spi.AnnotatedConstructor;

public abstract class ForwardingAnnotatedConstructor<X> extends ForwardingAnnotatedCallable<X> implements AnnotatedConstructor<X>
{

   @Override
   protected abstract AnnotatedConstructor<X> delegate();
   
   @Override
   public Constructor<X> getJavaMember()
   {
      return delegate().getJavaMember();
   }

}
