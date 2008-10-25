package org.jboss.webbeans.introspector;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class SimpleAnnotatedMethod<T> extends AbstractAnnotatedItem<T, Method> implements AnnotatedMethod<T>
{
   
   private static final Type[] actualTypeArguements = new Type[0];
   
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

   public Method getDelegate()
   {
      return method;
   }
   
   public Class<T> getType()
   {
      return (Class<T>) method.getReturnType();
   }
   
   public Type[] getActualTypeArguements()
   {
      return actualTypeArguements;
   }

}
