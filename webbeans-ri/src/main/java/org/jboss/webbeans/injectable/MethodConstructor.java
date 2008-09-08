package org.jboss.webbeans.injectable;

import java.lang.reflect.Method;

import javax.webbeans.Container;

public class MethodConstructor<T> extends InjectableMethod<T> implements
      ComponentConstructor<T>
{

   public MethodConstructor(Method method)
   {
      super(method);
   }

   public T invoke(Container container)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
