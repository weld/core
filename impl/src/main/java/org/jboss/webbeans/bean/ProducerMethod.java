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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Disposes;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.injection.ParameterInjectionPoint;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.introspector.WBParameter;
import org.jboss.webbeans.util.Names;

/**
 * Represents a producer method bean
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class ProducerMethod<T> extends AbstractProducerBean<T, Method>
{
   // The underlying method
   private MethodInjectionPoint<T, ?> method;

   private DisposalMethod<?> disposalMethodBean;

   private ProducerMethod<?> specializedBean;

   private final String id;

   /**
    * Creates a producer method Web Bean
    * 
    * @param method The underlying method abstraction
    * @param declaringBean The declaring bean abstraction
    * @param manager the current manager
    * @return A producer Web Bean
    */
   public static <T> ProducerMethod<T> of(WBMethod<T, ?> method, AbstractClassBean<?> declaringBean, BeanManagerImpl manager)
   {
      return new ProducerMethod<T>(method, declaringBean, manager);
   }

   protected ProducerMethod(WBMethod<T, ?> method, AbstractClassBean<?> declaringBean, BeanManagerImpl manager)
   {
      super(declaringBean, manager);
      this.method = MethodInjectionPoint.of(this, method);
      initType();
      initTypes();
      initBindings();
      this.id = createId("ProducerMethod-" + declaringBean.getType().getName() + "-" + method.getSignature().toString());
      initStereotypes();
      initPolicy();
   }

   public T produce(CreationalContext<T> creationalContext)
   {
      Object receiver = getReceiver(creationalContext);
      if (receiver != null)
      {
         return method.invokeOnInstance(receiver, manager, creationalContext, CreationException.class);
      }
      else
      {
         return method.invoke(receiver, manager, creationalContext, CreationException.class);
      }
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      if (!isInitialized())
      {
         initProducerMethodInjectableParameters();
         super.initialize(environment);
         checkProducerMethod();
         initDisposalMethod(environment);
      }
   }

   /**
    * Initializes the injection points
    */
   protected void initProducerMethodInjectableParameters()
   {
      for (WBParameter<?, ?> parameter : method.getWBParameters())
      {
         addInjectionPoint(ParameterInjectionPoint.of(this, parameter));
      }
   }

   /**
    * Validates the producer method
    */
   protected void checkProducerMethod()
   {
      if (getAnnotatedItem().getAnnotatedWBParameters(Observes.class).size() > 0)
      {
         throw new DefinitionException("Producer method cannot have parameter annotated @Observes");
      }
      else if (getAnnotatedItem().getAnnotatedWBParameters(Disposes.class).size() > 0)
      {
         throw new DefinitionException("Producer method cannot have parameter annotated @Disposes");
      }
      else if (getDeclaringBean() instanceof SessionBean<?>)
      {
         boolean methodDeclaredOnTypes = false;
         // TODO use annotated item?
         for (Type type : getDeclaringBean().getTypes())
         {
            if (type instanceof Class)
            {
               Class<?> clazz = (Class<?>) type;
               try
               {
                  clazz.getDeclaredMethod(getAnnotatedItem().getName(), getAnnotatedItem().getParameterTypesAsArray());
                  methodDeclaredOnTypes = true;
               }
               catch (NoSuchMethodException nsme)
               {
                  // No - op
               }
            }
         }
         if (!methodDeclaredOnTypes)
         {
            throw new DefinitionException("Producer method " + toString() + " must be declared on a business interface of " + getDeclaringBean());
         }
      }
   }

   /**
    * Initializes the remove method
    */
   protected void initDisposalMethod(BeanDeployerEnvironment environment)
   {
      Set<DisposalMethod<?>> disposalBeans = environment.resolveDisposalBeans(getTypes(), getQualifiers(), getDeclaringBean());

      if (disposalBeans.size() == 1)
      {
         this.disposalMethodBean = disposalBeans.iterator().next();
      }
      else if (disposalBeans.size() > 1)
      {
         // TODO List out found disposal methods
         throw new DefinitionException("Cannot declare multiple disposal methods for this producer method. Producer method: " + this + ". Disposal methods: " + disposalBeans);
      }
   }

   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      try
      {
         dispose(instance);
      }
      finally
      {
         if (getDeclaringBean().isDependent())
         {
            creationalContext.release();
         }
      }
   }
   
   public void dispose(T instance) 
   {
      if (disposalMethodBean != null)
      {
         disposalMethodBean.invokeDisposeMethod(instance);
      }
   }

   /**
    * Gets the annotated item representing the method
    * 
    * @return The annotated item
    */
   @Override
   public WBMethod<T, ?> getAnnotatedItem()
   {
      return method;
   }

   /**
    * Returns the default name
    * 
    * @return The default name
    */
   @Override
   protected String getDefaultName()
   {
      return method.getPropertyName();
   }

   /**
    * Returns the disposal method
    * 
    * @return The method representation
    */
   public DisposalMethod<?> getDisposalMethod()
   {
      return disposalMethodBean;
   }

   /**
    * Gets a string representation
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append(Names.scopeTypeToString(getScope()));
      if (getName() == null)
      {
         buffer.append("unnamed producer method bean");
      }
      else
      {
         buffer.append("simple producer method bean '" + getName() + "'");
      }
      buffer.append(" [" + getBeanClass().getName() + "] for class type [" + getType().getName() + "] API types " + getTypes() + ", binding types " + getQualifiers());
      return buffer.toString();
   }

   @Override
   public AbstractBean<?, ?> getSpecializedBean()
   {
      return specializedBean;
   }

   @Override
   protected void preSpecialize(BeanDeployerEnvironment environment)
   {
      if (getDeclaringBean().getAnnotatedItem().getWBSuperclass().getDeclaredWBMethod(getAnnotatedItem().getAnnotatedMethod()) == null)
      {
         throw new DefinitionException("Specialized producer method does not override a method on the direct superclass");
      }
   }

   @Override
   protected void specialize(BeanDeployerEnvironment environment)
   {
      WBMethod<?, ?> superClassMethod = getDeclaringBean().getAnnotatedItem().getWBSuperclass().getWBMethod(getAnnotatedItem().getAnnotatedMethod());
      if (environment.getProducerMethod(superClassMethod) == null)
      {
         throw new IllegalStateException(toString() + " does not specialize a bean");
      }
      this.specializedBean = environment.getProducerMethod(superClassMethod);
   }

   @Override
   public String getId()
   {
      return id;
   }

   public Set<Class<? extends Annotation>> getStereotypes()
   {
      return Collections.emptySet();
   }

}
