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

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.introspector.ConstructorSignature;
import org.jboss.weld.introspector.ForwardingWeldConstructor;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldParameter;

public class ConstructorInjectionPoint<T> extends ForwardingWeldConstructor<T> implements WeldInjectionPoint<T, Constructor<T>>, Serializable
{

   private static abstract class ForwardingParameterInjectionPointList<T, X> extends AbstractList<ParameterInjectionPoint<T, X>>
   {

      protected abstract List<? extends WeldParameter<T, X>> delegate();

      protected abstract Bean<X> declaringBean();;

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

   private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

   private final Bean<T> declaringBean;
   private final WeldConstructor<T> constructor;

   public static <T> ConstructorInjectionPoint<T> of(Bean<T> declaringBean, WeldConstructor<T> constructor)
   {
      return new ConstructorInjectionPoint<T>(declaringBean, constructor);
   }

   protected ConstructorInjectionPoint(Bean<T> declaringBean, WeldConstructor<T> constructor)
   {
      this.declaringBean = declaringBean;
      this.constructor = constructor;
   }

   @Override
   protected WeldConstructor<T> delegate()
   {
      return constructor;
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

   public T newInstance(BeanManagerImpl manager, CreationalContext<?> creationalContext)
   {
      try
      {
         return delegate().newInstance(getParameterValues(getWeldParameters(), null, null, manager, creationalContext));
      }
      catch (IllegalArgumentException e)
      {
         rethrowException(e);
      }
      catch (InstantiationException e)
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

   @Override
   public List<ParameterInjectionPoint<?, T>> getWeldParameters()
   {
      final List<? extends WeldParameter<?, T>> delegate = super.getWeldParameters();
      return new ForwardingParameterInjectionPointList()
      {

         @Override
         protected Bean<T> declaringBean()
         {
            return declaringBean;
         }

         @Override
         protected List<? extends WeldParameter<?, T>> delegate()
         {
            return delegate;
         }

      };
   }

   public void inject(Object declaringInstance, Object value)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Helper method for getting the current parameter values from a list of
    * annotated parameters.
    * 
    * @param parameters The list of annotated parameter to look up
    * @param manager The Bean manager
    * @return The object array of looked up values
    */
   protected Object[] getParameterValues(List<ParameterInjectionPoint<?, T>> parameters, 
         Object specialVal, Class<? extends Annotation> specialParam, 
         BeanManagerImpl manager, CreationalContext<?> creationalContext)
   {
      Object[] parameterValues = new Object[parameters.size()];
      Iterator<ParameterInjectionPoint<?, T>> iterator = parameters.iterator();
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

   public Type getType()
   {
      return getJavaClass();
   }

   public Member getMember()
   {
      return getJavaMember();
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
   
   // Serialization
   
   private Object writeReplace() throws ObjectStreamException
   {
      return new SerializationProxy<T>(this);
   }
   
   private void readObject(ObjectInputStream stream) throws InvalidObjectException
   {
      throw new InvalidObjectException("Proxy required");
   }
   
   private static class SerializationProxy<T> extends WeldInjectionPointSerializationProxy<T, Constructor<T>>
   {

      private static final long serialVersionUID = 9181171328831559650L;
      
      private final ConstructorSignature signature;

      public SerializationProxy(ConstructorInjectionPoint<T> injectionPoint)
      {
         super(injectionPoint);
         this.signature = injectionPoint.getSignature();
      }
      
      private Object readResolve()
      {
         return ConstructorInjectionPoint.of(getDeclaringBean(), getWeldConstructor());
      }
      
      protected WeldConstructor<T> getWeldConstructor()
      {
         return getWeldClass().getDeclaredWeldConstructor(signature);
      }
      
      @SuppressWarnings("unchecked")
      @Override
      protected WeldClass<T> getWeldClass()
      {
         return (WeldClass<T>) super.getWeldClass();
      }
      
   }


}
