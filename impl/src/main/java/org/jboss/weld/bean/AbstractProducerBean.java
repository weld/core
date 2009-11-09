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

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.NON_SERIALIZABLE_CONSTRUCTOR_PARAM_INJECTION_ERROR;
import static org.jboss.weld.logging.messages.BeanMessage.NON_SERIALIZABLE_FIELD_INJECTION_ERROR;
import static org.jboss.weld.logging.messages.BeanMessage.NON_SERIALIZABLE_INITIALIZER_PARAM_INJECTION_ERROR;
import static org.jboss.weld.logging.messages.BeanMessage.NON_SERIALIZABLE_PRODUCER_PARAM_INJECTION_ERROR;
import static org.jboss.weld.logging.messages.BeanMessage.NON_SERIALIZABLE_PRODUCT_ERROR;
import static org.jboss.weld.logging.messages.BeanMessage.NULL_NOT_ALLOWED_FROM_PRODUCER;
import static org.jboss.weld.logging.messages.BeanMessage.ONLY_ONE_SCOPE_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.PRODUCER_CAST_ERROR;
import static org.jboss.weld.logging.messages.BeanMessage.RETURN_TYPE_MUST_BE_CONCRETE;
import static org.jboss.weld.logging.messages.BeanMessage.TYPE_PARAMETER_MUST_BE_CONCRETE;
import static org.jboss.weld.logging.messages.BeanMessage.USING_DEFAULT_SCOPE;
import static org.jboss.weld.logging.messages.BeanMessage.USING_SCOPE;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;
import javax.inject.Inject;
import javax.inject.Scope;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.DefinitionException;
import org.jboss.weld.IllegalProductException;
import org.jboss.weld.WeldException;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.introspector.WeldMember;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Names;
import org.jboss.weld.util.Reflections;
import org.slf4j.cal10n.LocLogger;

/**
 * The implicit producer bean
 * 
 * @author Gavin King
 * 
 * @param <T>
 * @param <S>
 */
public abstract class AbstractProducerBean<X, T, S extends Member> extends AbstractReceiverBean<X, T, S>
{
   private static final LocLogger log = loggerFactory().getLogger(BEAN);
   
   private Producer<T> producer;
   private boolean passivationCapable;

   /**
    * Constructor
    * @param declaringBean The declaring bean
    * @param manager The Bean manager
    */
   public AbstractProducerBean(String idSuffix, AbstractClassBean<X> declaringBean, BeanManagerImpl manager)
   {
      super(idSuffix, declaringBean, manager);
   }

   @Override
   public abstract WeldMember<T, X, S> getAnnotatedItem();

   @Override
   // Overriden to provide the class of the bean that declares the producer method/field
   public Class<?> getBeanClass()
   {
      return getDeclaringBean().getBeanClass();
   }

   /**
    * Initializes the API types
    */
   @Override
   protected void initTypes()
   {
      if (getType().isArray() || getType().isPrimitive())
      {
         Set<Type> types = new HashSet<Type>();
         types.add(getType());
         types.add(Object.class);
         super.types = types;
      }
      else
      {
         super.initTypes();
      }
   }

   /**
    * Initializes the type
    */
   protected void initType()
   {
      try
      {
         this.type = getAnnotatedItem().getJavaClass();
      }
      catch (ClassCastException e)
      {
         Type type = Beans.getDeclaredBeanType(getClass());
         throw new WeldException(PRODUCER_CAST_ERROR, e, getAnnotatedItem().getJavaClass(), (type == null ? " unknown " : type));
      }
   }

   /**
    * Validates the producer method
    */
   protected void checkProducerReturnType()
   {
      if ((getAnnotatedItem().getBaseType() instanceof TypeVariable<?>) || 
          (getAnnotatedItem().getBaseType() instanceof WildcardType))
      {
         throw new DefinitionException(RETURN_TYPE_MUST_BE_CONCRETE, getAnnotatedItem().getBaseType());
      }
      for (Type type : getAnnotatedItem().getActualTypeArguments())
      {
         if (!(type instanceof Class))
         {
            throw new DefinitionException(TYPE_PARAMETER_MUST_BE_CONCRETE, this.getAnnotatedItem());
         }
      }
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      getDeclaringBean().initialize(environment);
      super.initialize(environment);
      checkProducerReturnType();
      initPassivationCapable();
      initAlternative();
   }
   
   private void initPassivationCapable()
   {
      if (getAnnotatedItem().isFinal() && !Serializable.class.isAssignableFrom(getAnnotatedItem().getJavaClass()))
      {
         this.passivationCapable = false;
      }
      else
      {
         this.passivationCapable = true;
      }
   }
   
   @Override
   protected void initAlternative()
   {
      super.alternative = Beans.isAlternative(getAnnotatedItem(), getMergedStereotypes()) || getDeclaringBean().isAlternative();
   }

   @Override
   public boolean isPassivationCapable()
   {
      return passivationCapable;
   }
   
   @Override
   public Set<InjectionPoint> getInjectionPoints()
   {
      return getProducer().getInjectionPoints();
   }

   /**
    * Validates the return value
    * 
    * @param instance The instance to validate
    */
   protected void checkReturnValue(T instance)
   {
      if (instance == null && !isDependent())
      {
         throw new IllegalProductException(NULL_NOT_ALLOWED_FROM_PRODUCER, getProducer());
      }
      else if (instance != null)
      {
         boolean passivating = manager.getServices().get(MetaAnnotationStore.class).getScopeModel(getScope()).isPassivating();
         if (passivating && !Reflections.isSerializable(instance.getClass()))
         {
            throw new IllegalProductException(NON_SERIALIZABLE_PRODUCT_ERROR, getProducer());
         }
         InjectionPoint injectionPoint = manager.getCurrentInjectionPoint();
         if (injectionPoint == null || injectionPoint.equals(BeanManagerImpl.DUMMY_INJECTION_POINT))
         {
            return;
         }
         if (!Reflections.isSerializable(instance.getClass()) && Beans.isPassivatingScope(injectionPoint.getBean(), manager))
         {
            if (injectionPoint.getMember() instanceof Field)
            {
               if (!injectionPoint.isTransient() && instance != null && !Reflections.isSerializable(instance.getClass()))
               {
                  throw new IllegalProductException(NON_SERIALIZABLE_FIELD_INJECTION_ERROR, this, injectionPoint);
               }
            }
            else if (injectionPoint.getMember() instanceof Method)
            {
               Method method = (Method) injectionPoint.getMember();
               if (method.isAnnotationPresent(Inject.class))
               {
                  throw new IllegalProductException(NON_SERIALIZABLE_INITIALIZER_PARAM_INJECTION_ERROR, this, injectionPoint);
               }
               if (method.isAnnotationPresent(Produces.class))
               {
                  throw new IllegalProductException(NON_SERIALIZABLE_PRODUCER_PARAM_INJECTION_ERROR, this, injectionPoint);
               }
            }
            else if (injectionPoint.getMember() instanceof Constructor)
            {
               throw new IllegalProductException(NON_SERIALIZABLE_CONSTRUCTOR_PARAM_INJECTION_ERROR, this, injectionPoint);
            }
         }
      }
   }

   @Override
   protected void initScopeType()
   {
      Set<Annotation> scopeAnnotations = new HashSet<Annotation>();
      scopeAnnotations.addAll(getAnnotatedItem().getMetaAnnotations(Scope.class));
      scopeAnnotations.addAll(getAnnotatedItem().getMetaAnnotations(NormalScope.class));
      if (scopeAnnotations.size() > 1)
      {
         throw new DefinitionException(ONLY_ONE_SCOPE_ALLOWED, getProducer());
      }
      if (scopeAnnotations.size() == 1)
      {
         this.scopeType = scopeAnnotations.iterator().next().annotationType();
         log.trace(USING_SCOPE, scopeType, this);
         return;
      }

      initScopeTypeFromStereotype();

      if (this.scopeType == null)
      {
         this.scopeType = Dependent.class;
         log.trace(USING_DEFAULT_SCOPE, this);
      }
   }
   
   /**
    * This operation is *not* threadsafe, and should not be called outside bootstrap
    * 
    * @param producer
    */
   public void setProducer(Producer<T> producer)
   {
      this.producer = producer;
   }
   
   public Producer<T> getProducer()
   {
      return producer;
   }

   /**
    * Creates an instance of the bean
    * 
    * @returns The instance
    */
   public T create(final CreationalContext<T> creationalContext)
   {
      try
      {
         T instance = getProducer().produce(creationalContext);
         checkReturnValue(instance);
         return instance;
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
    * Gets a string representation
    * 
    * @return The string representation
    */
   @Override
   public String getDescription()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Annotated " + Names.scopeTypeToString(getScope()));
      if (getName() == null)
      {
         buffer.append("unnamed producer bean");
      }
      else
      {
         buffer.append("simple producer bean '" + getName() + "'");
      }
      buffer.append(" [" + getBeanClass().getName() + "] for class type [" + getType().getName() + "] API types " + getTypes() + ", binding types " + getQualifiers());
      return buffer.toString();
   }

}