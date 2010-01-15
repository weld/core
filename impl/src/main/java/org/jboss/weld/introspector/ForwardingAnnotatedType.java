package org.jboss.weld.introspector;

import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

/**
 * Forwarding implementation of AnnotatedType
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
public abstract class ForwardingAnnotatedType<X> extends ForwardingAnnotated implements AnnotatedType<X>
{

   @Override
   protected abstract AnnotatedType<X> delegate();

   public Set<AnnotatedConstructor<X>> getConstructors()
   {
      return delegate().getConstructors();
   }

   public Set<AnnotatedField<? super X>> getFields()
   {
      return delegate().getFields();
   }

   public Class<X> getJavaClass()
   {
      return delegate().getJavaClass();
   }

   public Set<AnnotatedMethod<? super X>> getMethods()
   {
      return delegate().getMethods();
   }

}
