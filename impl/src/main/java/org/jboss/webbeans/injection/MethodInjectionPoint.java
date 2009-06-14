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
package org.jboss.webbeans.injection;

import static org.jboss.webbeans.injection.Exceptions.rethrowException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.introspector.ForwardingWBMethod;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.introspector.WBParameter;

public class MethodInjectionPoint<T> extends ForwardingWBMethod<T> implements WBInjectionPoint<T, Method>
{

   private abstract class ForwardingParameterInjectionPointList extends AbstractList<ParameterInjectionPoint<?>>
   {

      protected abstract List<? extends WBParameter<?>> delegate();

      protected abstract Bean<?> declaringBean();;

      @Override
      public ParameterInjectionPoint<?> get(int index)
      {
         return ParameterInjectionPoint.of(declaringBean, delegate().get(index));
      }

      @Override
      public int size()
      {
         return delegate().size();
      }

   }

   private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

   private final Bean<?> declaringBean;
   private final WBMethod<T> method;

   public static <T> MethodInjectionPoint<T> of(Bean<?> declaringBean, WBMethod<T> method)
   {
      return new MethodInjectionPoint<T>(declaringBean, method);
   }

   protected MethodInjectionPoint(Bean<?> declaringBean, WBMethod<T> method)
   {
      this.declaringBean = declaringBean;
      this.method = method;
   }

   @Override
   protected WBMethod<T> delegate()
   {
      return method;
   }

   public Bean<?> getBean()
   {
      return declaringBean;
   }

   public Set<Annotation> getBindings()
   {
      return delegate().getBindings();
   }

   public T invoke(Object declaringInstance, BeanManagerImpl manager, CreationalContext<?> creationalContext, Class<? extends RuntimeException> exceptionTypeToThrow)
   {
      try
      {
         return delegate().invoke(declaringInstance, getParameterValues(getParameters(), null, null, manager, creationalContext));
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
         return invoke(declaringInstance, getParameterValues(getParameters(), annotatedParameter, parameter, manager, creationalContext));
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
         return delegate().invokeOnInstance(declaringInstance, getParameterValues(getParameters(), null, null, manager, creationalContext));
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
         return invokeOnInstance(declaringInstance, getParameterValues(getParameters(), annotatedParameter, parameter, manager, creationalContext));
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
   public List<ParameterInjectionPoint<?>> getParameters()
   {
      final List<? extends WBParameter<?>> delegate = super.getParameters();
      return new ForwardingParameterInjectionPointList()
      {

         @Override
         protected Bean<?> declaringBean()
         {
            return declaringBean;
         }

         @Override
         protected List<? extends WBParameter<?>> delegate()
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
    * @param manager The Web Beans manager
    * @return The object array of looked up values
    */
   protected Object[] getParameterValues(List<ParameterInjectionPoint<?>> parameters, Class<? extends Annotation> specialParam, Object specialVal, BeanManagerImpl manager, CreationalContext<?> creationalContext)
   {
      Object[] parameterValues = new Object[parameters.size()];
      Iterator<ParameterInjectionPoint<?>> iterator = parameters.iterator();
      for (int i = 0; i < parameterValues.length; i++)
      {
         ParameterInjectionPoint<?> param = iterator.next();
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
      // TODO Auto-generated method stub
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

}
