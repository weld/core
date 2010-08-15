/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.container.weld.ee.embedded_1_1.mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.MessageDriven;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbDescriptor;

public class MockEjbDescriptor<T> implements EjbDescriptor<T>
{
   private final Class<T> beanClass;
   private final String ejbName;
   private final List<BusinessInterfaceDescriptor<?>> localInterfaces;
   private final List<BusinessInterfaceDescriptor<?>> remoteInterfaces;
   private final HashSet<Method> removeMethods;
   
   public static <T> MockEjbDescriptor<T> of(Class<T> type)
   {
      return new MockEjbDescriptor<T>(type);
   }

   private MockEjbDescriptor(final Class<T> type)
   {
      this.beanClass = type;
      this.ejbName = type.getSimpleName();
      
      this.localInterfaces = new ArrayList<BusinessInterfaceDescriptor<?>>();
      Local localAnnotation = type.getAnnotation(Local.class);
      if (localAnnotation != null)
      {
         for (final Class<?> clazz : localAnnotation.value())
         {
            localInterfaces.add(createBusinessInterfaceDescriptor(clazz));
         }
      }
      
      for (final Class<?> clazz : type.getInterfaces())
      {
         if (clazz.isAnnotationPresent(Local.class))
         {
            localInterfaces.add(createBusinessInterfaceDescriptor(clazz));
         }
      }
      
      this.remoteInterfaces = new ArrayList<BusinessInterfaceDescriptor<?>>();
      Remote remoteAnnotation = type.getAnnotation(Remote.class);
      if (remoteAnnotation != null)
      {
         for (final Class<?> clazz : remoteAnnotation.value())
         {
            remoteInterfaces.add(createBusinessInterfaceDescriptor(clazz));
         }
      }
      
      for (final Class<?> clazz : type.getInterfaces())
      {
         if (clazz.isAnnotationPresent(Remote.class))
         {
            remoteInterfaces.add(createBusinessInterfaceDescriptor(clazz));
         }
      }
      
      // cope with EJB 3.1 style no-interface views
      if (localInterfaces.size() == 0)
      {
         localInterfaces.add(createBusinessInterfaceDescriptor(type));
      }
      this.removeMethods = new HashSet<Method>();
      for (final Method method : type.getMethods())
      {
         if (method.isAnnotationPresent(Remove.class))
         {
            removeMethods.add(method);
         }
      }
   }

   private BusinessInterfaceDescriptor<Object> createBusinessInterfaceDescriptor(final Class<?> clazz)
   {
      return new BusinessInterfaceDescriptor<Object>()
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
      
      };
   }

   public String getEjbName()
   {
      return ejbName;
   }

   public Collection<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces()
   {
      return localInterfaces;
   }
   
   public Collection<BusinessInterfaceDescriptor<?>> getRemoteBusinessInterfaces()
   {
      return remoteInterfaces;
   }

   public Collection<Method> getRemoveMethods()
   {
      return removeMethods;
   }

   public Class<T> getBeanClass()
   {
      return beanClass;
   }

   public boolean isMessageDriven()
   {
      return beanClass.isAnnotationPresent(MessageDriven.class);
   }

   public boolean isSingleton()
   {
      return beanClass.isAnnotationPresent(Singleton.class);
   }

   public boolean isStateful()
   {
      return beanClass.isAnnotationPresent(Stateful.class);
   }

   public boolean isStateless()
   {
      return beanClass.isAnnotationPresent(Stateless.class);
   }
   
   public String getLocalJndiName()
   {
      return beanClass.getSimpleName() + "/local";
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
      builder.append("; BeanClass: " + getBeanClass() + "; Local Business Interfaces: " + getLocalBusinessInterfaces());
      return builder.toString(); 
   }
   
//   @Override
//   public boolean equals(Object other)
//   {
//      if (other instanceof EjbDescriptor)
//      {
//         EjbDescriptor<T> that = (EjbDescriptor<T>) other;
//         return this.getBeanClass().equals(that.getBeanClass());
//      }
//      else
//      {
//         return false;
//      }
//   }
//   
//   @Override
//   public int hashCode()
//   {
//      return getEjbName().hashCode();
//   }

}
