package org.jboss.webbeans.injectable;

import java.lang.reflect.Method;

import javax.webbeans.manager.Manager;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.SimpleAnnotatedMethod;

public class InjectableMethod<T> extends Unit<T, Method>
{

   private AnnotatedMethod<T> method;
   
   public InjectableMethod(java.lang.reflect.Method method)
   {
      super(method.getParameterTypes(), method.getParameterAnnotations());
      this.method = new SimpleAnnotatedMethod<T>(method);
   }

   @SuppressWarnings("unchecked")
   public T invoke(ManagerImpl manager, Object instance)
   {
      return invoke(manager, instance, getParameterValues(manager));
   }
   
   @SuppressWarnings("unchecked")
   public T invoke(Manager container, Object instance, Object[] parameters)
   {
      try
      {
         return (T) method.getAnnotatedMethod().invoke(instance, parameters);
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Unable to invoke " + method + " on " + instance, e);
      }
   }
   
   @Override
   public AnnotatedItem<T, Method> getAnnotatedItem()
   {
      return method;
   }
   
}
