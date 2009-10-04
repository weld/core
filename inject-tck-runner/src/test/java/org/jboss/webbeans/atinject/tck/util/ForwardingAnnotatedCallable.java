package org.jboss.webbeans.atinject.tck.util;

import java.util.List;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;

public abstract class ForwardingAnnotatedCallable<X> extends ForwardingAnnotatedMember<X> implements AnnotatedCallable<X>
{

   @Override
   protected abstract AnnotatedCallable<X> delegate();

   public List<AnnotatedParameter<X>> getParameters()
   {
      return delegate().getParameters();
   }

}
