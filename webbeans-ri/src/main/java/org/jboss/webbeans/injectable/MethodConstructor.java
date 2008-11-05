package org.jboss.webbeans.injectable;

import java.lang.reflect.Method;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedMethod;

public class MethodConstructor<T> extends InjectableMethod<T> implements
      BeanConstructor<T, AnnotatedMethod<T>>
{

   public MethodConstructor(Method method)
   {
      super(method);
   }

   @Override
   public AnnotatedMethod<T> getAnnotatedItem()
   {
      return null;
   }
   
   public T invoke(ManagerImpl manager)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
