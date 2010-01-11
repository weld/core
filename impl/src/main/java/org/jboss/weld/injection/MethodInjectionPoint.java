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
package org.jboss.weld.injection;

import static org.jboss.weld.injection.Exceptions.rethrowException;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.introspector.ForwardingWeldMethod;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.manager.BeanManagerImpl;

public class MethodInjectionPoint<T, X> extends ForwardingWeldMethod<T, X> implements WeldInjectionPoint<T, Method>
{

   private static abstract class ForwardingParameterInjectionPointList<T, X> extends AbstractList<ParameterInjectionPoint<T, X>>
   {

      protected abstract List<? extends WeldParameter<T, X>> delegate();

      protected abstract Bean<X> declaringBean();

      @Override
      public ParameterInjectionPoint<T, X> get(int index)
      {
         return ParameterInjectionPoint.of(declaringBean(), delegate().get(index));
      }

      @Override
      public int size()
      {
         return delegate().size();
      }

   }

   private final Bean<?> declaringBean;
   private final WeldMethod<T, X> method;

   public static <T, X> MethodInjectionPoint<T, X> of(Bean<?> declaringBean, WeldMethod<T, X> method)
   {
      return new MethodInjectionPoint<T, X>(declaringBean, method);
   }

   protected MethodInjectionPoint(Bean<?> declaringBean, WeldMethod<T, X> method)
   {
      this.declaringBean = declaringBean;
      this.method = method;
   }

   @Override
   protected WeldMethod<T, X> delegate()
   {
      return method;
   }

   public Bean<?> getBean()
   {
      return declaringBean;
   }

   @Override
   public Set<Annotation> getQualifiers()
   {
      return delegate().getQualifiers();
   }

   public T invoke(Object declaringInstance, BeanManagerImpl manager, CreationalContext<?> creationalContext, Class<? extends RuntimeException> exceptionTypeToThrow)
   {
      try
      {
         return delegate().invoke(declaringInstance, getParameterValues(getWeldParameters(), null, null, manager, creationalContext));
      }
      catch (IllegalArgumentException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      catch (IllegalAccessException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      catch (InvocationTargetException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public T invokeWithSpecialValue(Object declaringInstance, Class<? extends Annotation> annotatedParameter, Object parameter, BeanManagerImpl manager, CreationalContext<?> creationalContext, Class<? extends RuntimeException> exceptionTypeToThrow)
   {
      try
      {
         return invoke(declaringInstance, getParameterValues(getWeldParameters(), annotatedParameter, parameter, manager, creationalContext));
      }
      catch (IllegalArgumentException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      catch (IllegalAccessException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      catch (InvocationTargetException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      return null;
   }

   public T invokeOnInstance(Object declaringInstance, BeanManagerImpl manager, CreationalContext<?> creationalContext, Class<? extends RuntimeException> exceptionTypeToThrow)
   {
      try
      {
         return delegate().invokeOnInstance(declaringInstance, getParameterValues(getWeldParameters(), null, null, manager, creationalContext));
      }
      catch (IllegalArgumentException e)
      {
         rethrowException(e);
      }
      catch (IllegalAccessException e)
      {
         rethrowException(e);
      }
      catch (InvocationTargetException e)
      {
         rethrowException(e);
      }
      catch (SecurityException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      catch (NoSuchMethodException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public T invokeOnInstanceWithSpecialValue(Object declaringInstance, Class<? extends Annotation> annotatedParameter, Object parameter, BeanManagerImpl manager, CreationalContext<?> creationalContext, Class<? extends RuntimeException> exceptionTypeToThrow)
   {
      try
      {
         return invokeOnInstance(declaringInstance, getParameterValues(getWeldParameters(), annotatedParameter, parameter, manager, creationalContext));
      }
      catch (IllegalArgumentException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      catch (SecurityException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      catch (IllegalAccessException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      catch (InvocationTargetException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      catch (NoSuchMethodException e)
      {
         rethrowException(e, exceptionTypeToThrow);
      }
      return null;
   }

   @Override
   public List<ParameterInjectionPoint<?, X>> getWeldParameters()
   {
      final List<? extends WeldParameter<?, X>> delegate = super.getWeldParameters();
      return new ForwardingParameterInjectionPointList()
      {

         @Override
         protected Bean<?> declaringBean()
         {
            return declaringBean;
         }

         @Override
         protected List<? extends WeldParameter<?, X>> delegate()
         {
            return delegate;
         }

      };
   }

   public void inject(Object declaringInstance, Object value)
   {
      try
      {
         delegate().invoke(declaringInstance, value);
      }
      catch (IllegalArgumentException e)
      {
         rethrowException(e);
      }
      catch (IllegalAccessException e)
      {
         rethrowException(e);
      }
      catch (InvocationTargetException e)
      {
         rethrowException(e);
      }
   }

   /**
    * Helper method for getting the current parameter values from a list of
    * annotated parameters.
    * 
    * @param parameters The list of annotated parameter to look up
    * @param manager The Bean manager
    * @return The object array of looked up values
    */
   protected Object[] getParameterValues(List<ParameterInjectionPoint<?, X>> parameters, 
         Class<? extends Annotation> specialParam, Object specialVal, 
         BeanManagerImpl manager, CreationalContext<?> creationalContext)
   {
      Object[] parameterValues = new Object[parameters.size()];
      Iterator<ParameterInjectionPoint<?, X>> iterator = parameters.iterator();
      for (int i = 0; i < parameterValues.length; i++)
      {
         ParameterInjectionPoint<?, ?> param = iterator.next();
         if (specialParam != null && param.isAnnotationPresent(specialParam))
         {
            parameterValues[i] = specialVal;
         }
         else
         {
            parameterValues[i] = param.getValueToInject(manager, creationalContext);
         }
      }
      return parameterValues;
   }

   public Annotated getAnnotated()
   {
      return delegate();
   }

   public boolean isDelegate()
   {
      return false;
   }

   public boolean isTransient()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public Type getType()
   {
      return getBaseType();
   }

   public Member getMember()
   {
      return getJavaMember();
   }
   
   
   // Serialization
   
   private Object writeReplace() throws ObjectStreamException
   {
      return new SerializationProxy<T>(this);
   }
   
   private void readObject(ObjectInputStream stream) throws InvalidObjectException
   {
      throw new InvalidObjectException(PROXY_REQUIRED);
   }
   
   private static class SerializationProxy<T> extends WeldInjectionPointSerializationProxy<T, Method>
   {

      private static final long serialVersionUID = 9181171328831559650L;
      
      private final MethodSignature signature;

      public SerializationProxy(MethodInjectionPoint<T, ?> injectionPoint)
      {
         super(injectionPoint);
         this.signature = injectionPoint.getSignature();
      }
      
      private Object readResolve()
      {
         return MethodInjectionPoint.of(getDeclaringBean(), getWeldMethod());
      }
      
      protected WeldMethod<T, ?> getWeldMethod()
      {
         return getWeldClass().getDeclaredWeldMethod(signature);
      }
      
   }

}
