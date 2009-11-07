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
package org.jboss.weld.bean.builtin;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.BeanManagerImpl;

/**
 * Common implementation for binding-type-based helpers
 * 
 * @author Gavin King
 * 
 * @param <T>
 */
public abstract class AbstractFacade<T, X>
{
   
   protected static Type getFacadeType(InjectionPoint injectionPoint)
   {
      Type genericType = injectionPoint.getType();
      if (genericType instanceof ParameterizedType )
      {
         return ((ParameterizedType) genericType).getActualTypeArguments()[0];
      }
      else
      {
         throw new IllegalStateException("Must have concrete type argument " + injectionPoint);
      }
   }

   private final BeanManagerImpl beanManager;
   private final InjectionPoint injectionPoint;
   private final Type type;
   private final Annotation[] qualifiers;
   
   protected AbstractFacade(Type type, Annotation[] qualifiers, InjectionPoint injectionPoint, BeanManagerImpl beanManager)
   {
      this.beanManager = beanManager;
      this.injectionPoint = injectionPoint;
      this.type = type;
      this.qualifiers = qualifiers;
   }

   protected BeanManagerImpl getBeanManager()
   {
      return beanManager.getCurrent();
   }
   
   protected Annotation[] getQualifiers()
   {
      return qualifiers;
   }
   
   protected Type getType()
   {
      return type;
   }
   
   protected InjectionPoint getInjectionPoint()
   {
      return injectionPoint;
   }
   

   /**
    * Gets a string representation
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      return "Abstract facade implmentation";
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof AbstractFacade<?, ?>)
      {
         AbstractFacade<?, ?> that = (AbstractFacade<?, ?>) obj;
         return this.getType().equals(that.getType()) && Arrays.equals(this.getQualifiers(), that.getQualifiers());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      int hashCode = 17;
      hashCode += getType().hashCode() * 5;
      hashCode += Arrays.hashCode(getQualifiers()) * 7;
      return hashCode;
   }
   
   // Serialization

   protected static class AbstractFacadeSerializationProxy implements Serializable
   {
      
      private static final long serialVersionUID = -9118965837530101152L;
      
      private final InjectionPoint injectionPoint;
      private final BeanManagerImpl beanManager;
      
      protected AbstractFacadeSerializationProxy(AbstractFacade<?, ?> facade)
      {
         this.injectionPoint = facade.getInjectionPoint();
         this.beanManager = facade.getBeanManager();
      }
      
      protected BeanManagerImpl getBeanManager()
      {
         return beanManager;
      }
      
      protected InjectionPoint getInjectionPoint()
      {
         return injectionPoint;
      }
      
   }


}