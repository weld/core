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
package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.decorator.Decorates;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.BindingType;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Initializer;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bean.AbstractProducerBean;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.injection.ConstructorInjectionPoint;
import org.jboss.webbeans.injection.FieldInjectionPoint;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.injection.ParameterInjectionPoint;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.introspector.WBConstructor;
import org.jboss.webbeans.introspector.WBField;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.introspector.WBParameter;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.metadata.cache.BindingTypeModel;
import org.jboss.webbeans.metadata.cache.MetaAnnotationStore;

/**
 * Helper class for bean inspection
 * 
 * @author Pete Muir
 * @author David Allen
 *
 */
public class Beans
{
   
   private static final Log log = Logging.getLog(Beans.class);
   
   /**
    * Indicates if a bean's scope type is passivating
    * 
    * @param bean The bean to inspect
    * @return True if the scope is passivating, false otherwise
    */
   public static boolean isPassivatingScope(Bean<?> bean, BeanManagerImpl manager)
   {
      if (bean instanceof EnterpriseBean<?>)
      {
         return ((EnterpriseBean<?>) bean).getEjbDescriptor().isStateful();
      }
      else
      {
         return manager.getServices().get(MetaAnnotationStore.class).getScopeModel(bean.getScopeType()).isPassivating();
      }
   }

   /**
    * Tests if a bean is capable of having its state temporarily stored to
    * secondary storage
    * 
    * @param bean The bean to inspect
    * @return True if the bean is passivation capable
    */
   public static boolean isPassivationCapableBean(Bean<?> bean)
   {
      if (bean instanceof EnterpriseBean<?>)
      {
         return ((EnterpriseBean<?>) bean).getEjbDescriptor().isStateful();
      }
      else if (bean instanceof AbstractProducerBean<?, ?>)
      {
         return Reflections.isSerializable(((AbstractProducerBean<?, ?>) bean).getType());
      }
      else
      {
         return Reflections.isSerializable(bean.getBeanClass());
      }
   }

   /**
    * Indicates if a bean is proxyable
    * 
    * @param bean The bean to test
    * @return True if proxyable, false otherwise
    */
   public static boolean isBeanProxyable(Bean<?> bean)
   {
      if (bean instanceof RIBean<?>)
      {
         return ((RIBean<?>) bean).isProxyable();
      }
      else
      {
         return Proxies.isTypesProxyable(bean.getTypes());
      }
   }

   public static Set<FieldInjectionPoint<?, ?>> getFieldInjectionPoints(Bean<?> declaringBean, WBClass<?> annotatedItem)
   {
      Set<FieldInjectionPoint<?, ?>> injectableFields = new HashSet<FieldInjectionPoint<?, ?>>();
      for (WBField<?, ?> annotatedField : annotatedItem.getMetaAnnotatedWBFields(BindingType.class))
      {
         addFieldInjectionPoint(annotatedField, injectableFields, declaringBean);
      }
      for (WBField<?, ?> annotatedField : annotatedItem.getAnnotatedWBFields(Decorates.class))
      {
         addFieldInjectionPoint(annotatedField, injectableFields, declaringBean);
      }
      return injectableFields;
   }
   
   public static WBMethod<?, ?> getPostConstruct(WBClass<?> type)
   {
      Set<WBMethod<?, ?>> postConstructMethods = type.getAnnotatedWBMethods(PostConstruct.class);
      log.trace("Found " + postConstructMethods + " constructors annotated with @Initializer for " + type);
      if (postConstructMethods.size() > 1)
      {
         throw new DefinitionException("Cannot have more than one post construct method annotated with @PostConstruct for " + type);
      }
      else if (postConstructMethods.size() == 1)
      {
         WBMethod<?, ?> postConstruct = postConstructMethods.iterator().next();
         log.trace("Exactly one post construct method (" + postConstruct + ") for " + type);
         return postConstruct;
      }
      else
      {
         return null;
      }
   }
   
   public static WBMethod<?, ?> getPreDestroy(WBClass<?> type)
   {
      Set<WBMethod<?, ?>> preDestroyMethods = type.getAnnotatedWBMethods(PreDestroy.class);
      log.trace("Found " + preDestroyMethods + " constructors annotated with @Initializer for " + type);
      if (preDestroyMethods.size() > 1)
      {
         // TODO actually this is wrong, in EJB you can have @PreDestroy methods
         // on the superclass, though the Web Beans spec is silent on the issue
         throw new DefinitionException("Cannot have more than one pre destroy method annotated with @PreDestroy for " + type);
      }
      else if (preDestroyMethods.size() == 1)
      {
         WBMethod<?, ?> preDestroy = preDestroyMethods.iterator().next();
         log.trace("Exactly one post construct method (" + preDestroy + ") for " + type);
         return preDestroy;
      }
      else
      {
         return null;
      }
   }
   
   public static Set<MethodInjectionPoint<?, ?>> getInitializerMethods(Bean<?> declaringBean, WBClass<?> type)
   {
      Set<MethodInjectionPoint<?, ?>> initializerMethods = new HashSet<MethodInjectionPoint<?, ?>>();
      for (WBMethod<?, ?> method : type.getAnnotatedWBMethods(Initializer.class))
      {
         if (method.isStatic())
         {
            throw new DefinitionException("Initializer method " + method.toString() + " cannot be static on " + type);
         }
         else if (method.getAnnotation(Produces.class) != null)
         {
            throw new DefinitionException("Initializer method " + method.toString() + " cannot be annotated @Produces on " + type);
         }
         else if (method.getAnnotatedWBParameters(Disposes.class).size() > 0)
         {
            throw new DefinitionException("Initializer method " + method.toString() + " cannot have parameters annotated @Disposes on " + type);
         }
         else if (method.getAnnotatedWBParameters(Observes.class).size() > 0)
         {
            throw new DefinitionException("Initializer method " + method.toString() + " cannot be annotated @Observes on " + type);
         }
         else
         {
            MethodInjectionPoint<?, ?> initializerMethod = MethodInjectionPoint.of(declaringBean, method); 
            initializerMethods.add(initializerMethod);
         }
      }
      return initializerMethods;
   }
   
   public static Set<ParameterInjectionPoint<?, ?>> getParameterInjectionPoints(Bean<?> declaringBean, WBConstructor<?> constructor)
   {
      Set<ParameterInjectionPoint<?,?>> injectionPoints = new HashSet<ParameterInjectionPoint<?,?>>();
      for (WBParameter<?, ?> parameter : constructor.getWBParameters())
      {
         injectionPoints.add(ParameterInjectionPoint.of(declaringBean, parameter));
      }
      return injectionPoints;
   }
   
   public static Set<ParameterInjectionPoint<?, ?>> getParameterInjectionPoints(Bean<?> declaringBean, Set<MethodInjectionPoint<?, ?>> methodInjectionPoints)
   {
      Set<ParameterInjectionPoint<?, ?>> injectionPoints = new HashSet<ParameterInjectionPoint<?,?>>();
      for (MethodInjectionPoint<?, ?> method : methodInjectionPoints)
      {
         for (WBParameter<?, ?> parameter : method.getWBParameters())
         {
            injectionPoints.add(ParameterInjectionPoint.of(declaringBean, parameter));
         }
      }
      return injectionPoints;
   }
   
   private static void addFieldInjectionPoint(WBField<?, ?> annotatedField, Set<FieldInjectionPoint<?, ?>> injectableFields, Bean<?> declaringBean)
   {
      if (!annotatedField.isAnnotationPresent(Produces.class))
      {
         if (annotatedField.isStatic())
         {
            throw new DefinitionException("Don't place binding annotations on static fields " + annotatedField);
         }
         if (annotatedField.isFinal())
         {
            throw new DefinitionException("Don't place binding annotations on final fields " + annotatedField);
         }
         FieldInjectionPoint<?, ?> fieldInjectionPoint = FieldInjectionPoint.of(declaringBean, annotatedField);
         injectableFields.add(fieldInjectionPoint);
      }
   }
   
   /**
    * Checks if binding criteria fulfill all binding types
    * 
    * @param element The binding criteria to check
    * @param bindings2 The binding types to check
    * @return True if all matches, false otherwise
    */
   public static boolean containsAllBindings(Set<Annotation> bindings1, Set<Annotation> bindings2, BeanManagerImpl manager)
   {
      for (Annotation binding : bindings1)
      {
         BindingTypeModel<?> bindingType = manager.getServices().get(MetaAnnotationStore.class).getBindingTypeModel(binding.annotationType());
         boolean matchFound = false;
         // TODO Something wrong with annotation proxy hashcode in JDK/AnnotationLiteral hashcode, so always do a full check, don't use contains
         for (Annotation otherBinding : bindings2)
         {
            if (bindingType.isEqual(binding, otherBinding))
            {
               matchFound = true;
            }
         }
         if (!matchFound)
         {
            return false;
         }
      }
      return true;
   }
   

   /**
    * Retains only beans which have deployment type X.
    * 
    * The deployment type X is
    * 
    * @param <T>
    * @param beans The beans to filter
    * @param enabledDeploymentTypes The enabled deployment types
    * @return The filtered beans
    */
   public static <T extends Bean<?>> Set<T> retainEnabledPolicies(Set<T> beans, Collection<Class<?>> enabledPolicyClasses, Collection<Class<? extends Annotation>> enabledPolicySterotypes)
   {
      if (beans.size() == 0)
      {
         return beans;
      }
      else
      {
         Set<T> enabledBeans = new HashSet<T>();
         for (T bean : beans)
         {
            if (bean.isPolicy())
            {
               if (enabledPolicyClasses.contains(bean.getBeanClass()))
               {
                  enabledBeans.add(bean);
               }
               else
               {
                  for (Class<? extends Annotation> stereotype : bean.getStereotypes())
                  {
                     if (enabledPolicySterotypes.contains(stereotype))
                     {
                        enabledBeans.add(bean);
                     }
                  }
               }
            }
            else
            {
               enabledBeans.add(bean);
            }
         }
         return enabledBeans;
      }
   }
   
   public static boolean isTypePresent(Bean<?> bean, Type type)
   {
      type = Types.boxedType(type);
      for (Type beanType : bean.getTypes())
      {
         if (Types.boxedType(beanType).equals(type))
         {
            return true;
         }
      }
      return false;
   }
   
   /**
    * Check if any of the beans is a policy
    * 
    * @param beans
    * @return
    */
   public static <X> boolean isPolicyPresent(Set<Bean<? extends X>> beans)
   {
      for (Bean<?> bean : beans)
      {
         if (bean.isPolicy())
         {
            return true;
         }
      }
      return false;
   }
   
   /**
    * Check if bean is specialized by any of beans
    * 
    * @param bean
    * @param beans
    * @param specializedBeans
    * @return
    */
   public static <X> boolean isSpecialized(Bean<? extends X> bean, Set<Bean<? extends X>> beans, Map<Contextual<?>, Contextual<?>> specializedBeans)
   {
      if (specializedBeans.containsKey(bean))
      {
         if (beans.contains(specializedBeans.get(bean)))
         {
            return true;
         }
      }
      return false;
   }
   
   public static <T> ConstructorInjectionPoint<T> getBeanConstructor(Bean<?> declaringBean, WBClass<T> type)
   {
      ConstructorInjectionPoint<T> constructor = null;
      Set<WBConstructor<T>> initializerAnnotatedConstructors = type.getAnnotatedWBConstructors(Initializer.class);
      log.trace("Found " + initializerAnnotatedConstructors + " constructors annotated with @Initializer for " + type);
      if (initializerAnnotatedConstructors.size() > 1)
      {
         if (initializerAnnotatedConstructors.size() > 1)
         {
            throw new DefinitionException("Cannot have more than one constructor annotated with @Initializer for " + type);
         }
      }
      else if (initializerAnnotatedConstructors.size() == 1)
      {
         constructor = ConstructorInjectionPoint.of(declaringBean, initializerAnnotatedConstructors.iterator().next());
         log.trace("Exactly one constructor (" + constructor + ") annotated with @Initializer defined, using it as the bean constructor for " + type);
      }
      else if (type.getNoArgsWBConstructor() != null)
      {

         constructor = ConstructorInjectionPoint.of(declaringBean, type.getNoArgsWBConstructor());
         log.trace("Exactly one constructor (" + constructor + ") defined, using it as the bean constructor for " + type);
      }
      
      if (constructor == null)
      {
         throw new DefinitionException("Cannot determine constructor to use for " + type);
      }
      else
      {
         return constructor;
      }
   }
   
}
