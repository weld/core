package org.jboss.webbeans.test.mock;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.ejb.MessageDriven;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.jboss.webbeans.bootstrap.spi.BusinessInterfaceDescriptor;
import org.jboss.webbeans.bootstrap.spi.EjbDescriptor;
import org.jboss.webbeans.bootstrap.spi.MethodDescriptor;

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

   public Iterable<MethodDescriptor> getRemoveMethods()
   {
      Collection<MethodDescriptor> removeMethods = new HashSet<MethodDescriptor>();
      for (final Method method : type.getMethods())
      {
         if (method.isAnnotationPresent(Remove.class))
         {
            removeMethods.add(new MethodDescriptor()
            {
               
               public Class<?> getDeclaringClass()
               {
                  return type;
               }
               
               public String getMethodName()
               {
                  return method.getName();
               }
               
               public Class<?>[] getMethodParameterTypes()
               {
                  return method.getParameterTypes();
               }
               
            });
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
      return false;
      //return type.isAnnotationPresent(Singleton.class);
   }

   public boolean isStateful()
   {
      return type.isAnnotationPresent(Stateful.class);
   }

   public boolean isStateless()
   {
      return type.isAnnotationPresent(Stateless.class);
   }
   
   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append(getEjbName());
      if (isStateful())
      {
         builder.append(" (SFSB)");
      }
      if (isStateless())
      {
         builder.append(" (SLSB)");
      }
      if (isSingleton())
      {
         builder.append(" (Singleton)");
      }
      if (isMessageDriven())
      {
         builder.append(" (MDB)");
      }
      builder.append("; BeanClass: " + getType() + "; Local Business Interfaces: " + getLocalBusinessInterfaces());
      return builder.toString(); 
   }
   
   @Override
   public boolean equals(Object other)
   {
      if (other instanceof EjbDescriptor)
      {
         EjbDescriptor<T> that = (EjbDescriptor<T>) other;
         return this.getEjbName().equals(that.getEjbName());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return getEjbName().hashCode();
   }

}
