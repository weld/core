package org.jboss.webbeans.injectable;

import java.lang.reflect.Method;

import javax.webbeans.Container;

public class MethodMetaModel<T> extends UnitMetaModel<T>
{

   private Method method;
   
   public MethodMetaModel(Method method)
   {
      super(method.getParameterTypes(), method.getParameterAnnotations());
      this.method = method;
   }

   @SuppressWarnings("unchecked")
   public T invoke(Container container, Object instance)
   {
      try
      {
         return (T) method.invoke(instance, getParameterValues(container));
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Unable to invoke " + method + " on " + instance, e);
      }
   }
   
   public Method getMethod()
   {
      return method;
   }
   
   
}
