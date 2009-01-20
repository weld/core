package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public abstract class ForwardingAnnotatedClass<T> extends ForwardingAnnotatedItem<T, Class<T>> implements AnnotatedClass<T>
{

   protected abstract AnnotatedClass<T> delegate();

   public Set<AnnotatedConstructor<T>> getAnnotatedConstructors(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedConstructors(annotationType);
   }

   public Set<AnnotatedField<?>> getAnnotatedFields(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedFields(annotationType);
   }

   public Set<AnnotatedMethod<?>> getAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedMethods(annotationType);
   }

   public AnnotatedConstructor<T> getConstructor(List<Class<?>> arguments)
   {
      return delegate().getConstructor(arguments);
   }

   public Set<AnnotatedConstructor<T>> getConstructors()
   {
      return delegate().getConstructors();
   }

   public Set<AnnotatedField<?>> getDeclaredAnnotatedFields(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredAnnotatedFields(annotationType);
   }

   public Set<AnnotatedMethod<?>> getDeclaredAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredAnnotatedMethods(annotationType);
   }

   public Set<AnnotatedMethod<?>> getDeclaredMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredMethodsWithAnnotatedParameters(annotationType);
   }

   public Set<AnnotatedField<?>> getFields()
   {
      return delegate().getFields();
   }

   public Set<AnnotatedField<?>> getMetaAnnotatedFields(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getMetaAnnotatedFields(metaAnnotationType);
   }

   public AnnotatedMethod<?> getMethod(Method method)
   {
      return delegate().getMethod(method);
   }

   public Set<AnnotatedMethod<?>> getMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getMethodsWithAnnotatedParameters(annotationType);
   }

   public AnnotatedClass<?> getSuperclass()
   {
      return delegate().getSuperclass();
   }

   public boolean isNonStaticMemberClass()
   {
      return delegate().isNonStaticMemberClass();
   }

   public boolean isParameterizedType()
   {
      return delegate().isParameterizedType();
   }

   public boolean isEquivalent(Class<?> clazz)
   {
      return delegate().isEquivalent(clazz);
   }
   
   public AnnotatedClass<T> wrap(Set<Annotation> annotations)
   {
      throw new UnsupportedOperationException();
   }
   
}
