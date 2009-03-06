/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.webbeans.mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.MessageDriven;
import javax.ejb.Remove;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.jboss.webbeans.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;

public class MockEjbDescriptor<T> implements EjbDescriptor<T>
{
   private final Class<T> type;
   private final String ejbName;
   private final List<BusinessInterfaceDescriptor<?>> localInterfaces;
   private final HashSet<Method> removeMethods;
   
   public static <T> MockEjbDescriptor<T> of(Class<T> type)
   {
      return new MockEjbDescriptor<T>(type);
   }

   private MockEjbDescriptor(final Class<T> type)
   {
      this.type = type;
      this.ejbName = type.getSimpleName();
      this.localInterfaces = new ArrayList<BusinessInterfaceDescriptor<?>>();
      for (final Class<?> clazz : type.getInterfaces())
      {
         if (clazz.isAnnotationPresent(Local.class))
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
      }
      // cope with EJB 3.1 style no-interface views
      if (localInterfaces.size() == 0)
      {
         localInterfaces.add(new BusinessInterfaceDescriptor<Object>()
         {

            public Class<Object> getInterface()
            {
               return (Class<Object>) type;
            }

            public String getJndiName()
            {
               return type.getSimpleName() +"/local";
            }
            
         });
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

   public Iterable<Method> getRemoveMethods()
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
