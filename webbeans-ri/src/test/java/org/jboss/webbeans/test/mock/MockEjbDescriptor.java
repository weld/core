package org.jboss.webbeans.test.mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.ejb.MessageDriven;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.jboss.webbeans.bootstrap.spi.BusinessInterfaceDescriptor;
import org.jboss.webbeans.bootstrap.spi.EjbDescriptor;
import org.jboss.webbeans.bootstrap.spi.MethodDescriptor;
import org.jboss.webbeans.test.annotations.Singleton;

public class MockEjbDescriptor<T> implements EjbDescriptor<T>
{
   private final Class<T> type;
   private final String ejbName;
   private final List<BusinessInterfaceDescriptor<?>> localInterfaces;
   private final HashSet<MethodDescriptor> removeMethods;

   public MockEjbDescriptor(final Class<T> type)
   {
      this.type = type;
      this.ejbName = type.getSimpleName();
      this.localInterfaces = new ArrayList<BusinessInterfaceDescriptor<?>>();
      for (final Class<?> clazz : type.getInterfaces())
      {
         localInterfaces.add(new BusinessInterfaceDescriptor<Object>()
               {

                  @SuppressWarnings("unchecked")
                  public Class<Object> getInterface()
                  {
                     return (Class<Object>) clazz;
                  }

                  public String getJndiName()
                  {
                     return clazz.getSimpleName() + "/local";
                  }
            
               });
      }
      this.removeMethods = new HashSet<MethodDescriptor>();
      for (final Method method : type.getMethods())
      {
         if (method.isAnnotationPresent(Remove.class))
         {
            removeMethods.add(new MethodDescriptor()
            {
               
               public String getMethodName()
               {
                  return method.getName();
               }
               
               public Class<?>[] getMethodParameterTypes()
               {
                  return method.getParameterTypes();
               }
               
               @Override
               public String toString()
               {
                  return method.toString();
               }
               
            });
         }
      }
   }

   public String getEjbName()
   {
      return ejbName;
   }

   public Iterable<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces()
   {
      return localInterfaces;
   }
   
   public Iterable<BusinessInterfaceDescriptor<?>> getRemoteBusinessInterfaces()
   {
      return Collections.emptyList();
   }

   public Iterable<MethodDescriptor> getRemoveMethods()
   {

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
   
   public String getLocalJndiName()
   {
      return type.getSimpleName() + "/local";
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
      builder.append("remove methods; " + removeMethods + "; ");
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
