package org.jboss.webbeans.injectable;

import java.lang.reflect.Method;

import org.jboss.webbeans.ManagerImpl;

public class MethodConstructor<T> extends InjectableMethod<T> implements
      ComponentConstructor<T>
{

   public MethodConstructor(Method method)
   {
      super(method);
   }

   public T invoke(ManagerImpl manager)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
