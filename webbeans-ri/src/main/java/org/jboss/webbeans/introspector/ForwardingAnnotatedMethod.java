package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.jboss.webbeans.ManagerImpl;

public abstract class ForwardingAnnotatedMethod<T> extends ForwardingAnnotatedMember<T, Method> implements AnnotatedMethod<T>
{
   
   @Override
   protected abstract AnnotatedMethod<T> delegate();
   
   public Method getAnnotatedMethod()
   {
      return delegate().getAnnotatedMethod();
   }
   
   public List<AnnotatedParameter<?>> getAnnotatedParameters(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getAnnotatedParameters(metaAnnotationType);
   }

   public AnnotatedType<?> getDeclaringClass()
   {
      return delegate().getDeclaringClass();
   }

   public Class<?>[] getParameterTypesAsArray()
   {
      return delegate().getParameterTypesAsArray();
   }

   public List<AnnotatedParameter<?>> getParameters()
   {
      return delegate().getParameters();
   }

   public String getPropertyName()
   {
      return delegate().getPropertyName();
   }

   public T invoke(Object instance, ManagerImpl manager)
   {
      return delegate().invoke(instance, manager);
   }

   public T invoke(Object instance, Object... parameters)
   {
      return delegate().invoke(instance, parameters);
   }

   public T invokeOnInstance(Object instance, ManagerImpl manager)
   {
      return delegate().invokeOnInstance(instance, manager);
   }

   public T invokeWithSpecialValue(Object instance, Class<? extends Annotation> specialParam, Object specialVal, ManagerImpl manager)
   {
      return delegate().invokeWithSpecialValue(instance, specialParam, specialVal, manager);
   }

   public boolean isEquivalent(Method method)
   {
      return delegate().isEquivalent(method);
   }
   
   public AnnotatedMethod<T> wrap(Set<Annotation> annotations)
   {
      throw new UnsupportedOperationException();
   }
   
}
