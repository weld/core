package org.jboss.webbeans.test.mock;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.ejb.MessageDriven;
import javax.ejb.Remove;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.jboss.webbeans.bootstrap.spi.BusinessInterfaceDescriptor;
import org.jboss.webbeans.bootstrap.spi.EjbDescriptor;

public class MockEjbDescriptor<T> implements EjbDescriptor<T>
{
   private Class<T> type;
   private String ejbName;

   public MockEjbDescriptor(Class<T> type)
   {
      this.type = type;
      this.ejbName = type.getSimpleName() + "/local";
   }

   public String getEjbName()
   {
      return ejbName;
   }

   public Iterable<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces()
   {
      return Collections.emptyList();
   }
   
   public Iterable<BusinessInterfaceDescriptor<?>> getRemoteBusinessInterfaces()
   {
      return Collections.emptyList();
   }

   public Iterable<Method> getRemoveMethods()
   {
      Collection<Method> removeMethods = new HashSet<Method>();
      for (Method method : type.getMethods())
      {
         if (method.isAnnotationPresent(Remove.class))
         {
            removeMethods.add(method);
         }
      }
      return removeMethods;
   }

   public Class<T> getType()
   {
      return type;
   }

   public boolean isMessageDriven()
   {
      return type.isAnnotationPresent(MessageDriven.class);
   }

   public boolean isSingleton()
   {
      return type.isAnnotationPresent(Singleton.class);
   }

   public boolean isStateful()
   {
      return type.isAnnotationPresent(Stateful.class);
   }

   public boolean isStateless()
   {
      return type.isAnnotationPresent(Stateless.class);
   }

}
