package org.jboss.webbeans.introspector;

import java.lang.reflect.Method;

public class SimpleAnnotatedMethod<T> extends AbstractAnnotatedItem<T, Method> implements AnnotatedMethod<T>
{
   
   private Method method;
   
   public SimpleAnnotatedMethod(Method method)
   {
      super(buildAnnotationMap(method));
      this.method = method;
   }

   public Method getAnnotatedMethod()
   {
      return method;
   }
   
   @Override
   public String toString()
   {
      return method + " " + getAnnotatedMethod().toString();
   }

   public Method getDelegate()
   {
      return method;
   }
   
   @SuppressWarnings("unchecked")
   public Class<? extends T> getType()
   {
      return (Class<? extends T>) method.getReturnType();
   }

}
