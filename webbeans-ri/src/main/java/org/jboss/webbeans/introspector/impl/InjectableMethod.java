package org.jboss.webbeans.introspector.impl;

import javax.webbeans.manager.Manager;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedMethod;

public class InjectableMethod<T> extends Invokable<T, AnnotatedMethod<T>>
{

   private AnnotatedMethod<T> method;

   protected InjectableMethod(){}
   
   public InjectableMethod(java.lang.reflect.Method method)
   {
      this(new SimpleAnnotatedMethod<T>(method));
   }
   
   public InjectableMethod(AnnotatedMethod<T> annotatedMethod)
   {
      super(annotatedMethod.getParameters());
      this.method = annotatedMethod;
   }

   
   public T invoke(ManagerImpl manager, Object instance)
   {
      return invoke(manager, instance, getParameterValues(manager));
   }
   
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
   
   public AnnotatedMethod<T> getAnnotatedItem()
   {
      return method;
   }
   
}
