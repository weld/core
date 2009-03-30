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
import java.util.Set;

import javax.context.CreationalContext;
import javax.event.Observes;
import javax.inject.CreationException;
import javax.inject.DefinitionException;
import javax.inject.Disposes;

import org.jboss.webbeans.RootManager;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.injection.ParameterInjectionPoint;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.util.Names;

/**
 * Represents a producer method bean
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class ProducerMethodBean<T> extends AbstractProducerBean<T, Method>
{
   // The underlying method
   private MethodInjectionPoint<T> method;
   
   private AnnotatedMethod<?> disposalMethod;
   
   private ProducerMethodBean<?> specializedBean;

   private final String id;
   
   /**
    * Creates a producer method Web Bean
    * 
    * @param method
    *           The underlying method abstraction
    * @param declaringBean
    *           The declaring bean abstraction
    * @param manager
    *           the current manager
    * @return A producer Web Bean
    */
   public static <T> ProducerMethodBean<T> of(AnnotatedMethod<T> method, AbstractClassBean<?> declaringBean, RootManager manager)
   {
      return new ProducerMethodBean<T>(method, declaringBean, manager);
   }
   
   protected ProducerMethodBean(AnnotatedMethod<T> method, AbstractClassBean<?> declaringBean, RootManager manager)
   {
      super(declaringBean, manager);
      this.method = MethodInjectionPoint.of(this, method);
      initType();
      initTypes();
      initBindings();
      this.id = createId("ProducerField-" + declaringBean.getType().getName() + "-"+ method.getSignature().toString());
   }
   
   protected T produceInstance(CreationalContext<T> creationalContext)
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
         super.initialize(environment);
         checkProducerMethod();
         // initDisposalMethod();
         initInjectionPoints();
      }
   }
   
   /**
    * Initializes the injection points
    */
   protected void initInjectionPoints()
   {
      for (AnnotatedParameter<?> parameter : method.getParameters())
      {
         injectionPoints.add(ParameterInjectionPoint.of(this, parameter));
      }
   }
   
   /**
    * Validates the producer method
    */
   protected void checkProducerMethod()
   {
      if (getAnnotatedItem().getAnnotatedParameters(Observes.class).size() > 0)
      {
         throw new DefinitionException("Producer method cannot have parameter annotated @Observes");
      }
      else if (getAnnotatedItem().getAnnotatedParameters(Disposes.class).size() > 0)
      {
         throw new DefinitionException("Producer method cannot have parameter annotated @Disposes");
      }
      else if (declaringBean instanceof EnterpriseBean)
      {
         boolean methodDeclaredOnTypes = false;
         // TODO use annotated item?
         for (Type type : declaringBean.getTypes())
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
            throw new DefinitionException("Producer method " + toString() + " must be declared on a business interface of " + declaringBean);
         }
      }
   }
   
   /**
    * Initializes the remove method
    */
   protected void initDisposalMethod(BeanDeployerEnvironment environment)
   {
      Set<AnnotatedMethod<?>> disposalMethods = manager.resolveDisposalMethods(getType(), getBindings().toArray(new Annotation[0]));
      if (disposalMethods.size() == 1)
      {
         this.disposalMethod = disposalMethods.iterator().next();
      }
      else if (disposalMethods.size() > 1)
      {
         // TODO List out found disposal methods
         throw new DefinitionException("Cannot declare multiple disposal methods for this producer method");
      }
   }
   
   /**
    * Gets the annotated item representing the method
    * 
    * @return The annotated item
    */
   @Override
   public AnnotatedMethod<T> getAnnotatedItem()
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
   public AnnotatedMethod<?> getDisposalMethod()
   {
      return disposalMethod;
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
      buffer.append(Names.scopeTypeToString(getScopeType()));
      if (getName() == null)
      {
         buffer.append("unnamed producer method bean");
      }
      else
      {
         buffer.append("simple producer method bean '" + getName() + "'");
      }
      buffer.append(" [" + getType().getName() + "] ");
      buffer.append("API types " + getTypes() + ", binding types " + getBindings());
      return buffer.toString();
   }

   @Override
   public AbstractBean<?, ?> getSpecializedBean()
   {
      return specializedBean;
   }
   
   @Override
   protected void preSpecialize()
   {
      if (declaringBean.getAnnotatedItem().getSuperclass().getDeclaredMethod(getAnnotatedItem().getAnnotatedMethod()) == null)
      {
         throw new DefinitionException("Specialized producer method does not override a method on the direct superclass");
      }
   }
   
   @Override
   protected void specialize(BeanDeployerEnvironment environment)
   {
      AnnotatedMethod<?> superClassMethod = declaringBean.getAnnotatedItem().getSuperclass().getMethod(getAnnotatedItem().getAnnotatedMethod());
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
   
}
