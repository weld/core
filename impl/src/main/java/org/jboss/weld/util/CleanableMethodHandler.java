package org.jboss.weld.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

import org.jboss.weld.Container;

public class CleanableMethodHandler implements MethodHandler, Serializable
{
   
   private static final long serialVersionUID = 2140367342468307705L;
   
   private MethodHandler delegate;

   public CleanableMethodHandler(MethodHandler delegate)
   {
      this.delegate = delegate;
      Container.instance().deploymentServices().get(JavassistCleaner.class).add(this);
   }
   
   public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable
   {
      return delegate.invoke(self, thisMethod, proceed, args);
   }
   
   public void clean()
   {
      this.delegate = null;
   }
   
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      Container.instance().deploymentServices().get(JavassistCleaner.class).add(this);
   }

}
