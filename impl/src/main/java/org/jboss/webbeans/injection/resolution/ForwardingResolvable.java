package org.jboss.webbeans.injection.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

public abstract class ForwardingResolvable implements Resolvable
{
   
   protected abstract Resolvable delegate();

   public Set<Annotation> getBindings()
   {
      return delegate().getBindings();
   }
   
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return delegate().isAnnotationPresent(annotationType);
   }
   
   public Set<Type> getTypeClosure()
   {
      return delegate().getTypeClosure();
   }
   
   public boolean isAssignableTo(Class<?> clazz)
   {
      return delegate().isAssignableTo(clazz);
   }

}
