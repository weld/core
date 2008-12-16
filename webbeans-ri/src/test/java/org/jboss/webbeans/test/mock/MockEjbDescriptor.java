package org.jboss.webbeans.test.mock;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.jboss.webbeans.bootstrap.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.EJB;

public class MockEjbDescriptor<T> implements EjbDescriptor<T>
{
   private Class<T> type;
   private String ejbName;

   public MockEjbDescriptor(String ejbName, Class<T> type)
   {
      this.ejbName = ejbName;
      this.type = type;
   }

   public String getEjbName()
   {
      return ejbName;
   }

   public Iterator<BusinessInterfaceDescriptor> getLocalBusinessInterfaces()
   {
      return new HashSet<BusinessInterfaceDescriptor>().iterator();
   }

   public Iterator<BusinessInterfaceDescriptor> getRemoteBusinessInterfaces()
   {
      return new HashSet<BusinessInterfaceDescriptor>().iterator();
   }

   public Iterator<Method> getRemoveMethods()
   {
      Collection<Method> removeMethods = new HashSet<Method>();
      for (Method method : type.getMethods())
      {
         if (method.isAnnotationPresent(EJB.REMOVE_ANNOTATION))
         {
            removeMethods.add(method);
         }
      }
      return removeMethods.iterator();
   }

   public Class<T> getType()
   {
      return type;
   }

   public boolean isMessageDriven()
   {
      return type.isAnnotationPresent(EJB.MESSAGE_DRIVEN_ANNOTATION);
   }

   public boolean isSingleton()
   {
      return type.isAnnotationPresent(EJB.SINGLETON_ANNOTATION);
   }

   public boolean isStateful()
   {
      return type.isAnnotationPresent(EJB.STATEFUL_ANNOTATION);
   }

   public boolean isStateless()
   {
      return type.isAnnotationPresent(EJB.STATELESS_ANNOTATION);
   }

}
