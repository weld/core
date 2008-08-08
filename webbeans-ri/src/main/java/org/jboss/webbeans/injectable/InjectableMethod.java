package org.jboss.webbeans.injectable;

import javax.webbeans.Container;

// TODO Name this class better
public class InjectableMethod<T> extends Unit<T>
{

   private java.lang.reflect.Method method;
   
   public InjectableMethod(java.lang.reflect.Method method)
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
   
   public java.lang.reflect.Method getMethod()
   {
      return method;
   }
   
   
}
