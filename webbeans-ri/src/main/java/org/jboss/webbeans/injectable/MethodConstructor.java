package org.jboss.webbeans.injectable;

import java.lang.reflect.Method;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.util.Reflections;

public class MethodConstructor<T> extends InjectableMethod<T> implements
      BeanConstructor<T, AnnotatedMethod<T>>
{

   public MethodConstructor(Method method)
   {
      super(method);
   }
   
   public T invoke(ManagerImpl manager, Object instance)
   {
      return (T) Reflections.invokeAndWrap(getAnnotatedItem().getDelegate(), instance, getParameterValues(manager));
   }

}
