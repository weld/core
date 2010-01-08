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
package org.jboss.weld.bean;

import static org.jboss.weld.logging.messages.BeanMessage.ABSTRACT_METHOD_MUST_MATCH_DECORATED_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.DECORATED_TYPE_PARAMETERIZED_DELEGATE_NOT;
import static org.jboss.weld.logging.messages.BeanMessage.DELEGATE_MUST_SUPPORT_EVERY_DECORATED_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.DELEGATE_ON_NON_INITIALIZER_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.DELEGATE_TYPE_PARAMETER_MISMATCH;
import static org.jboss.weld.logging.messages.BeanMessage.NO_DELEGATE_FOR_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.TOO_MANY_DELEGATES_FOR_DECORATOR;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Decorator;
import javax.inject.Inject;

import org.jboss.weld.bean.proxy.AbstractDecoratorMethodHandler;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.ProxyClassConstructorInjectionPointWrapper;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.introspector.jlr.WeldConstructorImpl;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Deployers;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.reflection.Reflections;

public class DecoratorImpl<T> extends ManagedBean<T> implements WeldDecorator<T>
{
   private WeldClass<?> annotatedDelegateItem;

   private WeldClass<T> proxyClassForAbstractDecorators;
   private WeldConstructor<T> constructorForAbstractDecorator;

   private Set<MethodSignature> decoratedMethodSignatures;

   public static <T> Decorator<T> wrap(final Decorator<T> decorator)
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
   public static <T> DecoratorImpl<T> of(WeldClass<T> clazz, BeanManagerImpl manager)
   {
      return new DecoratorImpl<T>(clazz, manager);
   }

   private WeldInjectionPoint<?, ?> delegateInjectionPoint;
   private Set<Annotation> delegateBindings;
   private Type delegateType;
   private Set<Type> delegateTypes;
   private Set<Type> decoratedTypes;
   private HashSet<WeldClass<?>> annotatedDecoratedTypes;

   protected DecoratorImpl(WeldClass<T> type, BeanManagerImpl manager)
   {
      super(type, new StringBuilder().append(Decorator.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(type.getName()).toString(), manager);
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
         checkAbstractMethods();
      }
   }

   protected void initDecoratedTypes()
   {
      this.decoratedTypes = new HashSet<Type>();
      this.decoratedTypes.addAll(getAnnotatedItem().getInterfaceOnlyFlattenedTypeHierarchy());
      this.decoratedTypes.remove(Serializable.class);

      this.decoratedMethodSignatures = Deployers.getDecoratedMethodSignatures(getManager(), this.decoratedTypes);
   }

   protected void initDelegateInjectionPoint()
   {
      this.delegateInjectionPoint = getDelegateInjectionPoints().iterator().next();
   }

   @Override
   protected void checkDelegateInjectionPoints()
   {
      for (WeldInjectionPoint<?, ?> injectionPoint : getDelegateInjectionPoints())
      {
         if (injectionPoint instanceof MethodInjectionPoint<?, ?> && !injectionPoint.isAnnotationPresent(Inject.class))
         {
            throw new DefinitionException(DELEGATE_ON_NON_INITIALIZER_METHOD, injectionPoint);
         }
      }
      if (getDelegateInjectionPoints().size() == 0)
      {
         throw new DefinitionException(NO_DELEGATE_FOR_DECORATOR, this);
      }
      else if (getDelegateInjectionPoints().size() > 1)
      {
         throw new DefinitionException(TOO_MANY_DELEGATES_FOR_DECORATOR, this);
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
         if (decoratedType instanceof Class<?>)
         {
            if (!((Class<?>) decoratedType).isAssignableFrom(delegateInjectionPoint.getJavaClass()))
            {
               throw new DefinitionException(DELEGATE_MUST_SUPPORT_EVERY_DECORATED_TYPE, decoratedType, this );
            }
         }
         else if (decoratedType instanceof ParameterizedType)
         {
            ParameterizedType parameterizedType = (ParameterizedType) decoratedType;
            if (!delegateInjectionPoint.isParameterizedType())
            {
               throw new DefinitionException(DECORATED_TYPE_PARAMETERIZED_DELEGATE_NOT, delegateType, this);
            }
            if (!Arrays.equals(delegateInjectionPoint.getActualTypeArguments(), parameterizedType.getActualTypeArguments()))
            {
               throw new DefinitionException(DELEGATE_TYPE_PARAMETER_MISMATCH, decoratedType, this );
            }
            Type rawType = ((ParameterizedType) decoratedType).getRawType();
            if (rawType instanceof Class<?> && !((Class<?>) rawType).isAssignableFrom(delegateInjectionPoint.getJavaClass()))
            {
               throw new DefinitionException(DELEGATE_MUST_SUPPORT_EVERY_DECORATED_TYPE, decoratedType, this );
            }
         }
      }
      annotatedDelegateItem = WeldClassImpl.of(delegateInjectionPoint.getJavaClass(), manager.getServices().get(ClassTransformer.class));
   }

   private void checkAbstractMethods()
   {
      if (getAnnotatedItem().isAbstract())
      {
         for(WeldMethod<?,?> method: getAnnotatedItem().getWeldMethods())
         {
            if (Reflections.isAbstract(((AnnotatedMethod) method).getJavaMember()))
            {
               MethodSignature methodSignature = method.getSignature();
               if (this.annotatedDelegateItem.getWeldMethod(methodSignature) == null)
               {
                  throw new DefinitionException(ABSTRACT_METHOD_MUST_MATCH_DECORATED_TYPE,  method.getSignature(), this, getAnnotatedItem().getName());
               }
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
   
   public WeldInjectionPoint<?, ?> getDelegateInjectionPoint()
   {
      return delegateInjectionPoint;
   }

   @Override
   protected void initType()
   {
      super.initType();
      if (getAnnotatedItem().isAbstract())
      {
         Proxies.TypeInfo typeInfo = Proxies.TypeInfo.of(Collections.singleton(getAnnotatedItem().getJavaClass()));
         Class<T> clazz = Proxies.createProxyClass(null, typeInfo);
         proxyClassForAbstractDecorators = manager.getServices().get(ClassTransformer.class).loadClass(clazz);
      }
   }

   @Override
   protected void initConstructor()
   {
      super.initConstructor();
      if (getAnnotatedItem().isAbstract())
      {
         constructorForAbstractDecorator = WeldConstructorImpl.of(
               proxyClassForAbstractDecorators.getDeclaredWeldConstructor(getConstructor().getSignature()),
               proxyClassForAbstractDecorators,
               manager.getServices().get(ClassTransformer.class));
      }
   }

   @Override
   public void initDecorators()
   {
      // No-op, decorators can't have decorators
   }

   @Override
   protected T createInstance(CreationalContext<T> ctx)
   {
      if (!getAnnotatedItem().isAbstract())
      {
         return super.createInstance(ctx);
      }
      else
      {
         ProxyClassConstructorInjectionPointWrapper<T> constructorInjectionPointWrapper = new ProxyClassConstructorInjectionPointWrapper(this, constructorForAbstractDecorator, getConstructor());
         T instance = constructorInjectionPointWrapper.newInstance(manager, ctx);
         Proxies.attachMethodHandler(instance, new AbstractDecoratorMethodHandler(annotatedDelegateItem, getDelegateInjectionPoint(), constructorInjectionPointWrapper.getInjectedDelegate()));
         return instance;
      }
   }

   @Override
   public String getDescription()
   {
      // TODO Auto-generated method stub
      return super.getDescription("decorator");
   }

   public Set<MethodSignature> getDecoratedMethodSignatures()
   {
      return decoratedMethodSignatures;
   }
}
