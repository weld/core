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
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.context.CreationalContext;
import javax.inject.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.ForwardingAnnotatedMethod;
import org.jboss.webbeans.util.Reflections;

public class MethodInjectionPoint<T> extends ForwardingAnnotatedMethod<T> implements AnnotatedInjectionPoint<T, Method>
{
   
   private abstract class ForwardingParameterInjectionPointList extends AbstractList<ParameterInjectionPoint<?>>
   {
      
      protected abstract List<? extends AnnotatedParameter<?>> delegate();
      
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
   private final AnnotatedMethod<T> method;

   public static <T> MethodInjectionPoint<T> of(Bean<?> declaringBean, AnnotatedMethod<T> method)
   {
      return new MethodInjectionPoint<T>(declaringBean, method);
   }
   
   protected MethodInjectionPoint(Bean<?> declaringBean, AnnotatedMethod<T> method)
   {
      this.declaringBean = declaringBean;
      this.method = method;
   }
   
   @Override
   protected AnnotatedMethod<T> delegate()
   {
      return method;
   }

   public Annotation[] getAnnotations()
   {
      return delegate().getAnnotationStore().getAnnotations().toArray(EMPTY_ANNOTATION_ARRAY);
   }

   public Bean<?> getBean()
   {
      return declaringBean;
   }

   public Set<Annotation> getBindings()
   {
      return delegate().getBindings();
   }
   
   public T invoke(Object declaringInstance, ManagerImpl manager, CreationalContext<?> creationalContext)
   {
      try
      {
         return delegate().invoke(declaringInstance, getParameterValues(getParameters(), null, null, manager, creationalContext));
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
      return null;
   }
   
   @SuppressWarnings("unchecked")
   public T invokeWithSpecialValue(Object declaringInstance, Class<? extends Annotation> annotatedParameter, Object parameter, ManagerImpl manager, CreationalContext<?> creationalContext, Class<? extends RuntimeException> exceptionTypeToThrow)
   {
      try
      {
         if (delegate().getDeclaringClass().getType().equals(declaringInstance.getClass()))
         {
            return delegate().invoke(declaringInstance, getParameterValues(getParameters(), annotatedParameter, parameter, manager, creationalContext));
         }
         else
         {
            Method proxiedMethod = Reflections.lookupMethod(delegate().getAnnotatedMethod(), declaringInstance);
            return (T) proxiedMethod.invoke(declaringInstance, getParameterValues(getParameters(), annotatedParameter, parameter, manager, creationalContext));
         }
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
   
   @Override
   public List<ParameterInjectionPoint<?>> getParameters()
   {
      final List<? extends AnnotatedParameter<?>> delegate = super.getParameters();
      return new ForwardingParameterInjectionPointList()
      {

         @Override
         protected Bean<?> declaringBean()
         {
            return declaringBean;
         }

         @Override
         protected List<? extends AnnotatedParameter<?>> delegate()
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
   protected Object[] getParameterValues(List<ParameterInjectionPoint<?>> parameters, Class<? extends Annotation> specialParam, Object specialVal, ManagerImpl manager, CreationalContext<?> creationalContext)
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
   
}
