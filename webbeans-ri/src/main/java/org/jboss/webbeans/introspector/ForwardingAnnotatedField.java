package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import javax.webbeans.manager.Manager;

public abstract class ForwardingAnnotatedField<T> extends ForwardingAnnotatedMember<T, Field> implements AnnotatedField<T>
{

   @Override
   protected abstract AnnotatedField<T> delegate();

   public T get(Object instance)
   {
      return delegate().get(instance);
   }

   public Field getAnnotatedField()
   {
      return delegate().getAnnotatedField();
   }

   public AnnotatedType<?> getDeclaringClass()
   {
      return delegate().getDeclaringClass();
   }

   public String getPropertyName()
   {
      return delegate().getPropertyName();
   }

   public void inject(Object declaringInstance, Manager manager)
   {
      delegate().inject(declaringInstance, manager);
   }

   public void inject(Object declaringInstance, Object value)
   {
      delegate().inject(declaringInstance, value);
   }

   public void injectIntoInstance(Object declaringInstance, Object value)
   {
      delegate().injectIntoInstance(declaringInstance, value);
   }

   public void injectIntoInstance(Object declaringInstance, Manager manager)
   {
      delegate().injectIntoInstance(declaringInstance, manager);
   }

   public boolean isTransient()
   {
      return delegate().isTransient();
   }

   public AnnotatedField<T> wrap(Set<Annotation> annotations)
   {
      throw new UnsupportedOperationException();
   }
   
      
}
