package org.jboss.weld.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.InterceptionType;

import org.jboss.weld.bean.AbstractClassBean;

public class ForwardingInterceptorResolvable implements InterceptorResolvable
{

   public InterceptionType getInterceptionType()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <A extends Annotation> A getAnnotation(Class<A> annotationType)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public AbstractClassBean<?> getDeclaringBean()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Class<?> getJavaClass()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Set<Annotation> getQualifiers()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Set<Type> getTypeClosure()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isAssignableTo(Class<?> clazz)
   {
      // TODO Auto-generated method stub
      return false;
   }

}
