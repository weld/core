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
package org.jboss.webbeans.bean;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.Decorator;
import javax.inject.Inject;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.injection.WBInjectionPoint;
import org.jboss.webbeans.introspector.WBClass;

public class DecoratorBean<T> extends SimpleBean<T> implements Decorator<T>
{

   public static <T> Decorator<T> wrapForResolver(final Decorator<T> decorator)
   {
      return new ForwardingDecorator<T>()
      {

         @Override
         public Set<Annotation> getQualifiers()
         {
            return delegate().getDelegateQualifiers();
         }

         @Override
         public Set<Type> getTypes()
         {
            return delegate().getTypes();
         }

         @Override
         protected Decorator<T> delegate()
         {
            return decorator;
         }

      };
   }

   /**
    * Creates a decorator bean
    * 
    * @param <T> The type
    * @param clazz The class
    * @param manager the current manager
    * @return a Bean
    */
   public static <T> DecoratorBean<T> of(WBClass<T> clazz, BeanManagerImpl manager)
   {
      return new DecoratorBean<T>(clazz, manager);
   }

   private WBInjectionPoint<?, ?> delegateInjectionPoint;
   private Set<Annotation> delegateBindings;
   private Type delegateType;
   private Set<Type> delegateTypes;
   private Set<Type> decoratedTypes;

   protected DecoratorBean(WBClass<T> type, BeanManagerImpl manager)
   {
      super(type, manager);
   }

   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      if (!isInitialized())
      {
         super.initialize(environment);
         initDelegateInjectionPoint();
         initDecoratedTypes();
         initDelegateBindings();
         initDelegateType();
         checkDelegateType();
      }
   }

   protected void initDecoratedTypes()
   {
      this.decoratedTypes = new HashSet<Type>();
      this.decoratedTypes.addAll(getAnnotatedItem().getInterfaceOnlyFlattenedTypeHierarchy());
      this.decoratedTypes.remove(Serializable.class);
   }

   protected void initDelegateInjectionPoint()
   {
      this.delegateInjectionPoint = getDelegateInjectionPoints().iterator().next();
   }

   @Override
   protected void checkDelegateInjectionPoints()
   {
      for (WBInjectionPoint<?, ?> injectionPoint : getDelegateInjectionPoints())
      {
         if (injectionPoint instanceof MethodInjectionPoint<?, ?> && !injectionPoint.isAnnotationPresent(Inject.class))
         {
            throw new DefinitionException("Method with @Decorates parameter must be an initializer method " + injectionPoint);
         }
      }
      if (getDelegateInjectionPoints().size() == 0)
      {
         throw new DefinitionException("No delegate injection points defined " + this);
      }
      else if (getDelegateInjectionPoints().size() > 1)
      {
         throw new DefinitionException("Too many delegate injection point defined " + this);
      }
   }

   protected void initDelegateBindings()
   {
      this.delegateBindings = new HashSet<Annotation>(); 
      this.delegateBindings.addAll(this.delegateInjectionPoint.getQualifiers());
   }

   protected void initDelegateType()
   {
      this.delegateType = this.delegateInjectionPoint.getBaseType();
      this.delegateTypes = new HashSet<Type>();
      delegateTypes.add(delegateType);
   }

   protected void checkDelegateType()
   {
      for (Type decoratedType : getDecoratedTypes())
      {
         if (decoratedType instanceof Class)
         {
            if (!((Class<?>) decoratedType).isAssignableFrom(delegateInjectionPoint.getJavaClass()))
            {
               throw new DefinitionException("The delegate type must extend or implement every decorated type. Decorated type " + decoratedType + "." + this );
            }
         }
         else if (decoratedType instanceof ParameterizedType)
         {
            ParameterizedType parameterizedType = (ParameterizedType) decoratedType;
            if (!delegateInjectionPoint.isParameterizedType())
            {
               throw new DefinitionException("The decorated type is parameterized, but the delegate type isn't. Delegate type " + delegateType + "." + this);
            }
            if (!Arrays.equals(delegateInjectionPoint.getActualTypeArguments(), parameterizedType.getActualTypeArguments()));
            Type rawType = ((ParameterizedType) decoratedType).getRawType();
            if (rawType instanceof Class && !((Class<?>) rawType).isAssignableFrom(delegateInjectionPoint.getJavaClass()))
            {
               throw new DefinitionException("The delegate type must extend or implement every decorated type. Decorated type " + decoratedType + "." + this );
            }
            else
            {
               throw new IllegalStateException("Unable to process " + decoratedType);
            }

         }
      }
   }

   public Set<Annotation> getDelegateQualifiers()
   {
      return delegateBindings;
   }

   public Type getDelegateType()
   {
      return delegateType;
   }

   public Set<Type> getDecoratedTypes()
   {
      return decoratedTypes;
   }
   
   public WBInjectionPoint<?, ?> getDelegateInjectionPoint()
   {
      return delegateInjectionPoint;
   }

   /**
    * The type closure of the delegate type
    * 
    * @return the delegateTypes
    */
   public Set<Type> getDelegateTypes()
   {
      return delegateTypes;
   }
   
   @Override
   protected void initDecorators()
   {
      // No-op, decorators can't have decorators
   }
   
   /* (non-Javadoc)
    * @see org.jboss.webbeans.bean.SimpleBean#toString()
    */
   @Override
   public String toString()
   {
      // TODO Auto-generated method stub
      return super.toString("decorator");
   }

}
