package org.jboss.weld.atinject.tck.util;

import java.lang.reflect.Field;

import javax.enterprise.inject.spi.AnnotatedField;

public abstract class ForwardingAnnotatedField<X> extends ForwardingAnnotatedMember<X> implements AnnotatedField<X>
{

   @Override
   protected abstract AnnotatedField<X> delegate();
   
   @Override
   public Field getJavaMember()
   {
      return delegate().getJavaMember();
   }

}
