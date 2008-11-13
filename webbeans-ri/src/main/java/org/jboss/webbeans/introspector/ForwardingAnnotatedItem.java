package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

public abstract class ForwardingAnnotatedItem<T, S> implements AnnotatedItem<T, S>
{
   
   public Type[] getActualTypeArguments()
   {
      return delegate().getActualTypeArguments();
   }
   
   public <A extends Annotation> A getAnnotation(Class<? extends A> annotationType)
   {
      return delegate().getAnnotation(annotationType);
   }
   
   public <A extends Annotation> Set<A> getAnnotations()
   {
      return delegate().getAnnotations();
   }
   
   public Set<Annotation> getAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getAnnotations(metaAnnotationType);
   }
   
   public Annotation[] getAnnotationsAsArray(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getAnnotationsAsArray(metaAnnotationType);
   }
   
   public Set<Annotation> getBindingTypes()
   {
      return delegate().getBindingTypes();
   }
   
   public Annotation[] getBindingTypesAsArray()
   {
      return delegate().getBindingTypesAsArray();
   }
   
   public S getDelegate()
   {
      return delegate().getDelegate();
   }
   
   public String getName()
   {
      return delegate().getName();
   }
   
   public Class<T> getType()
   {
      return delegate().getType();
   }
   
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return delegate().isAnnotationPresent(annotationType);
   }
   
   public boolean isAssignableFrom(AnnotatedItem<?, ?> that)
   {
      return delegate().isAssignableFrom(that);
   }
   
   public boolean isAssignableFrom(Set<Class<?>> types)
   {
      return delegate().isAssignableFrom(types);
   }
   
   public boolean isFinal()
   {
      return delegate().isFinal();
   }
   
   public boolean isStatic()
   {
      return delegate().isStatic();
   }
   
   public boolean isProxyable()
   {
      return delegate().isProxyable();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }
   
   public abstract AnnotatedItem<T, S> delegate();
   
}
