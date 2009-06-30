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

import javax.enterprise.inject.Initializer;
import javax.enterprise.inject.spi.Decorator;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.injection.WBInjectionPoint;
import org.jboss.webbeans.introspector.WBAnnotated;
import org.jboss.webbeans.introspector.WBClass;

public class DecoratorBean<T> extends SimpleBean<T> implements Decorator<T>
{

   public static <T> Decorator<T> wrapForResolver(final Decorator<T> decorator)
   {
      return new ForwardingDecorator<T>()
      {

         @Override
         public Set<Annotation> getBindings()
         {
            return delegate().getDelegateBindings();
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

   private WBAnnotated<?, ?> decorates;
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
         initDelegate();
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

   protected void initDelegate()
   {
      this.decorates = getDecoratesInjectionPoint().iterator().next();
   }

   @Override
   protected void checkDecorates()
   {
      for (WBInjectionPoint<?, ?> injectionPoint : getDecoratesInjectionPoint())
      {
         if (injectionPoint instanceof MethodInjectionPoint && !injectionPoint.isAnnotationPresent(Initializer.class))
         {
            throw new DefinitionException("Method with @Decorates parameter must be an initializer method " + injectionPoint);
         }
      }
      if (getDecoratesInjectionPoint().size() == 0)
      {
         throw new DefinitionException("No @Decorates injection point defined " + this);
      }
      else if (getDecoratesInjectionPoint().size() > 1)
      {
         throw new DefinitionException("Too many @Decorates injection point defined " + this);
      }
   }

   protected void initDelegateBindings()
   {
      this.delegateBindings = new HashSet<Annotation>(); 
      this.delegateBindings.addAll(this.decorates.getBindings());
   }

   protected void initDelegateType()
   {
      this.delegateType = this.decorates.getBaseType();
      this.delegateTypes = new HashSet<Type>();
      delegateTypes.add(delegateType);
   }

   protected void checkDelegateType()
   {
      for (Type decoratedType : getDecoratedTypes())
      {
         if (decoratedType instanceof Class)
         {
            if (!((Class<?>) decoratedType).isAssignableFrom(decorates.getJavaClass()))
            {
               throw new DefinitionException("The delegate type must extend or implement every decorated type. Decorated type " + decoratedType + "." + this );
            }
         }
         else if (decoratedType instanceof ParameterizedType)
         {
            ParameterizedType parameterizedType = (ParameterizedType) decoratedType;
            if (!decorates.isParameterizedType())
            {
               throw new DefinitionException("The decorated type is parameterized, but the delegate type isn't. Delegate type " + delegateType + "." + this);
            }
            if (!Arrays.equals(decorates.getActualTypeArguments(), parameterizedType.getActualTypeArguments()));
            Type rawType = ((ParameterizedType) decoratedType).getRawType();
            if (rawType instanceof Class && !((Class<?>) rawType).isAssignableFrom(decorates.getJavaClass()))
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

   public Set<Annotation> getDelegateBindings()
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

}
