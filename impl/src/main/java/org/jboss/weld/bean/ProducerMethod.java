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

import static org.jboss.weld.logging.messages.BeanMessage.INCONSISTENT_ANNOTATIONS_ON_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.METHOD_NOT_BUSINESS_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.MULTIPLE_DISPOSAL_METHODS;
import static org.jboss.weld.logging.messages.BeanMessage.PRODUCER_METHOD_NOT_SPECIALIZING;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.ForbiddenStateException;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * Represents a producer method bean
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class ProducerMethod<X, T> extends AbstractProducerBean<X, T, Method>
{
   // The underlying method
   private MethodInjectionPoint<T, X> method;
   private DisposalMethod<X, ?> disposalMethodBean;
   private ProducerMethod<?, ?> specializedBean;
   private final String id;

   /**
    * Creates a producer method Web Bean
    * 
    * @param method The underlying method abstraction
    * @param declaringBean The declaring bean abstraction
    * @param beanManager the current manager
    * @return A producer Web Bean
    */
   public static <X, T> ProducerMethod<X, T> of(WeldMethod<T, X> method, AbstractClassBean<X> declaringBean, BeanManagerImpl beanManager)
   {
      return new ProducerMethod<X, T>(method, declaringBean, beanManager);
   }

   protected ProducerMethod(WeldMethod<T, X> method, AbstractClassBean<X> declaringBean, BeanManagerImpl beanManager)
   {
      super(new StringBuilder().append(ProducerMethod.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(declaringBean.getWeldAnnotated().getName()).append(".").append(method.getSignature().toString()).toString(), declaringBean, beanManager);
      this.method = MethodInjectionPoint.of(this, method);
      initType();
      initTypes();
      initQualifiers();
      this.id = new StringBuilder().append(BEAN_ID_PREFIX).append(getClass().getSimpleName()).append(BEAN_ID_SEPARATOR).append(declaringBean.getWeldAnnotated().getName()).append(getWeldAnnotated().getSignature().toString()).toString();
      initStereotypes();
      initProducerMethodInjectableParameters();
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
         initDisposalMethod(environment);
         setProducer(new Producer<T>()
         {

            public void dispose(T instance)
            {
               if (disposalMethodBean != null)
               {
                  disposalMethodBean.invokeDisposeMethod(instance);
               }
            }

            public Set<InjectionPoint> getInjectionPoints()
            {
               return (Set) getWeldInjectionPoints();
            }

            public T produce(CreationalContext<T> creationalContext)
            {
               Object receiver = getReceiver(creationalContext);
               if (receiver != null)
               {
                  return method.invokeOnInstance(receiver, beanManager, creationalContext, CreationException.class);
               }
               else
               {
                  return method.invoke(receiver, beanManager, creationalContext, CreationException.class);
               }
            }
            
            
            
         });
      }
   }

   /**
    * Initializes the injection points
    */
   protected void initProducerMethodInjectableParameters()
   {
      for (WeldParameter<?, ?> parameter : method.getWeldParameters())
      {
         addInjectionPoint(ParameterInjectionPoint.of(this, parameter));
      }
   }

   /**
    * Validates the producer method
    */
   protected void checkProducerMethod()
   {
      if (getWeldAnnotated().getWeldParameters(Observes.class).size() > 0)
      {
         throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Produces", "@Observes");
      }
      else if (getWeldAnnotated().getWeldParameters(Disposes.class).size() > 0)
      {
         throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Produces", "@Disposes");
      }
      else if (getDeclaringBean() instanceof SessionBean<?>)
      {
         boolean methodDeclaredOnTypes = false;
         // TODO use annotated item?
         for (Type type : getDeclaringBean().getTypes())
         {
            if (type instanceof Class<?>)
            {
               if (SecureReflections.methodExists((Class<?>) type, getWeldAnnotated().getName(), getWeldAnnotated().getParameterTypesAsArray()))
               {
                  methodDeclaredOnTypes = true;
                  continue;
               }
            }
         }
         if (!methodDeclaredOnTypes)
         {
            throw new DefinitionException(METHOD_NOT_BUSINESS_METHOD, this, getDeclaringBean());
         }
      }
   }

   /**
    * Initializes the remove method
    */
   protected void initDisposalMethod(BeanDeployerEnvironment environment)
   {
      Set<DisposalMethod<X, ?>> disposalBeans = environment.<X>resolveDisposalBeans(getTypes(), getQualifiers(), getDeclaringBean());

      if (disposalBeans.size() == 1)
      {
         this.disposalMethodBean = disposalBeans.iterator().next();
      }
      else if (disposalBeans.size() > 1)
      {
         throw new DefinitionException(MULTIPLE_DISPOSAL_METHODS, this, disposalBeans);
      }
   }

   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      try
      {
         getProducer().dispose(instance);
      }
      finally
      {
         if (getDeclaringBean().isDependent())
         {
            creationalContext.release();
         }
      }
   }

   /**
    * Gets the annotated item representing the method
    * 
    * @return The annotated item
    */
   @Override
   public WeldMethod<T, X> getWeldAnnotated()
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
   public DisposalMethod<X, ?> getDisposalMethod()
   {
      return disposalMethodBean;
   }

   @Override
   public AbstractBean<?, ?> getSpecializedBean()
   {
      return specializedBean;
   }

   @Override
   protected void preSpecialize(BeanDeployerEnvironment environment)
   {
      if (getDeclaringBean().getWeldAnnotated().getWeldSuperclass().getDeclaredWeldMethod(getWeldAnnotated().getJavaMember()) == null)
      {
         throw new DefinitionException(PRODUCER_METHOD_NOT_SPECIALIZING, this);
      }
   }

   @Override
   protected void specialize(BeanDeployerEnvironment environment)
   {
      WeldMethod<?, ?> superClassMethod = getDeclaringBean().getWeldAnnotated().getWeldSuperclass().getWeldMethod(getWeldAnnotated().getJavaMember());
      if (environment.getProducerMethod(superClassMethod) == null)
      {
         throw new ForbiddenStateException(PRODUCER_METHOD_NOT_SPECIALIZING, this);
      }
      this.specializedBean = environment.getProducerMethod(superClassMethod);
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public Set<Class<? extends Annotation>> getStereotypes()
   {
      return Collections.emptySet();
   }

}
