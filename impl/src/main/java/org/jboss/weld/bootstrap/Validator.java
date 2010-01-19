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
package org.jboss.weld.bootstrap;

import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_BEAN_CLASS_NOT_ANNOTATED;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_BEAN_CLASS_SPECIFIED_MULTIPLE_TIMES;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_STEREOTYPE_NOT_ANNOTATED;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_STEREOTYPE_SPECIFIED_MULTIPLE_TIMES;
import static org.jboss.weld.logging.messages.ValidatorMessage.AMBIGUOUS_EL_NAME;
import static org.jboss.weld.logging.messages.ValidatorMessage.BEAN_NAME_IS_PREFIX;
import static org.jboss.weld.logging.messages.ValidatorMessage.BEAN_SPECIALIZED_TOO_MANY_TIMES;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATOR_CLASS_NOT_BEAN_CLASS_OF_DECORATOR;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATOR_SPECIFIED_TWICE;
import static org.jboss.weld.logging.messages.ValidatorMessage.DISPOSAL_METHODS_WITHOUT_PRODUCER;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_INTO_NON_BEAN;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_INTO_NON_DEPENDENT_BEAN;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_HAS_AMBIGUOUS_DEPENDENCIES;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_HAS_NON_PROXYABLE_DEPENDENCIES;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_HAS_NON_SERIALIZABLE_DEPENDENCY;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_HAS_NULLABLE_DEPENDENCIES;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_HAS_UNSATISFIED_DEPENDENCIES;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_HAS_WILDCARD;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_MUST_HAVE_TYPE_PARAMETER;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_WITH_TYPE_VARIABLE;
import static org.jboss.weld.logging.messages.ValidatorMessage.INTERCEPTOR_NOT_ANNOTATED_OR_REGISTERED;
import static org.jboss.weld.logging.messages.ValidatorMessage.INTERCEPTOR_SPECIFIED_TWICE;
import static org.jboss.weld.logging.messages.ValidatorMessage.NEW_WITH_QUALIFIERS;
import static org.jboss.weld.logging.messages.ValidatorMessage.NON_SERIALIZABLE_BEAN_INJECTED_INTO_PASSIVATING_BEAN;
import static org.jboss.weld.logging.messages.ValidatorMessage.NOT_PROXYABLE;
import static org.jboss.weld.logging.messages.ValidatorMessage.PASSIVATING_BEAN_WITH_NONSERIALIZABLE_DECORATOR;
import static org.jboss.weld.logging.messages.ValidatorMessage.PASSIVATING_BEAN_WITH_NONSERIALIZABLE_INTERCEPTOR;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.interceptor.model.InterceptionModel;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.NewManagedBean;
import org.jboss.weld.bean.NewSessionBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.WeldDecorator;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalProductException;
import org.jboss.weld.exceptions.InconsistentSpecializationException;
import org.jboss.weld.exceptions.NullableDependencyException;
import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.exceptions.UnserializableDependencyException;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resolution.ResolvableWeldClass;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * Checks a list of beans for DeploymentExceptions and their subclasses
 * 
 * @author Nicklas Karlsson
 * @author David Allen
 */
public class Validator implements Service
{

   private void validateBean(Bean<?> bean, BeanManagerImpl beanManager)
   {
      for (InjectionPoint ij : bean.getInjectionPoints())
      {
         validateInjectionPoint(ij, beanManager);
      }
      boolean normalScoped = beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(bean.getScope()).isNormal();
      if (normalScoped && !Beans.isBeanProxyable(bean))
      {
         throw new UnproxyableResolutionException(NOT_PROXYABLE, bean);
      }
   }

   /**
    * Validate an RIBean. This includes validating whether two beans specialize
    * the same bean
    * 
    * @param bean the bean to validate
    * @param beanManager the current manager
    * @param specializedBeans the existing specialized beans
    */
   private void validateRIBean(RIBean<?> bean, BeanManagerImpl beanManager, Collection<RIBean<?>> specializedBeans)
   {
      validateBean(bean, beanManager);
      if (!(bean instanceof NewManagedBean<?>) && !(bean instanceof NewSessionBean<?>))
      {
         RIBean<?> abstractBean = bean;
         if (abstractBean.isSpecializing())
         {
            if (specializedBeans.contains(abstractBean.getSpecializedBean()))
            {
               throw new InconsistentSpecializationException(BEAN_SPECIALIZED_TOO_MANY_TIMES, bean);
            }
            specializedBeans.add(abstractBean.getSpecializedBean());
         }
         if ((bean instanceof AbstractClassBean<?>) && bean.isPassivationCapableBean())
         {
            AbstractClassBean<?> classBean = (AbstractClassBean<?>) bean;
            if (classBean.hasDecorators())
            {
               validateDecorators(beanManager, classBean);
            }
            // validate CDI-defined interceptors
            if (classBean.hasCdiBoundInterceptors())
            {
               validateCdiBoundInterceptors(beanManager, classBean);
            }
            // validate EJB-defined interceptors
            if (((AbstractClassBean<?>) bean).hasDirectlyDefinedInterceptors())
            {
               validateDirectlyDefinedInterceptorClasses(beanManager, classBean);
            }
         }
      }
   }

   @SuppressWarnings("unchecked")
   private void validateDirectlyDefinedInterceptorClasses(BeanManagerImpl beanManager, AbstractClassBean<?> classBean)
   {
      InterceptionModel<Class<?>, Class<?>> ejbInterceptorModel = beanManager.getClassDeclaredInterceptorsRegistry().getInterceptionModel(classBean.getType());
      if (ejbInterceptorModel != null)
      {
         Class<?>[] classDeclaredInterceptors = ejbInterceptorModel.getAllInterceptors().toArray(new Class<?>[ejbInterceptorModel.getAllInterceptors().size()]);
         if (classDeclaredInterceptors != null)
         {
            for (Class<?> interceptorClass : classDeclaredInterceptors)
            {
               if (!Reflections.isSerializable(interceptorClass))
               {
                  throw new DeploymentException(PASSIVATING_BEAN_WITH_NONSERIALIZABLE_INTERCEPTOR, this, interceptorClass.getName());
               }
               InjectionTarget<Object> injectionTarget = (InjectionTarget<Object>) beanManager.createInjectionTarget(beanManager.createAnnotatedType(interceptorClass));
               for (InjectionPoint injectionPoint : injectionTarget.getInjectionPoints())
               {
                  Bean<?> resolvedBean = beanManager.resolve(beanManager.getInjectableBeans(injectionPoint));
                  validateInjectionPointPassivationCapable(injectionPoint, resolvedBean, beanManager);
               }
            }
         }
      }
   }

   private void validateCdiBoundInterceptors(BeanManagerImpl beanManager, AbstractClassBean<?> classBean)
   {
      InterceptionModel<Class<?>, SerializableContextual<Interceptor<?>, ?>> cdiInterceptorModel = beanManager.getCdiInterceptorsRegistry().getInterceptionModel(classBean.getType());
      if (cdiInterceptorModel != null)
      {
         Collection<SerializableContextual<Interceptor<?>, ?>> interceptors = cdiInterceptorModel.getAllInterceptors();
         if (interceptors.size() > 0)
         {
            for (SerializableContextual<Interceptor<?>, ?> serializableContextual : interceptors)
            {
               if (!((InterceptorImpl<?>)serializableContextual.get()).isSerializable())
               {
                  throw new DeploymentException(PASSIVATING_BEAN_WITH_NONSERIALIZABLE_INTERCEPTOR, classBean, serializableContextual.get());
               }
               for (InjectionPoint injectionPoint : serializableContextual.get().getInjectionPoints())
               {
                  Bean<?> resolvedBean = beanManager.resolve(beanManager.getInjectableBeans(injectionPoint));
                  validateInjectionPointPassivationCapable(injectionPoint, resolvedBean, beanManager);
               }
            }
         }
      }
   }

   private void validateDecorators(BeanManagerImpl beanManager, AbstractClassBean<?> classBean)
   {
      for (Decorator<?> decorator : classBean.getDecorators())
      {
         if (!((WeldDecorator<?>)decorator).getWeldAnnotated().isSerializable())
         {
            throw new UnserializableDependencyException(PASSIVATING_BEAN_WITH_NONSERIALIZABLE_DECORATOR, classBean, decorator);
         }
         for (InjectionPoint ij : decorator.getInjectionPoints())
         {
            Bean<?> resolvedBean = beanManager.resolve(beanManager.getInjectableBeans(ij));
            validateInjectionPointPassivationCapable(ij, resolvedBean, beanManager);
         }
      }
   }

   /**
    * Validate an injection point
    * 
    * @param ij the injection point to validate
    * @param declaringBean the bean into which the injectionPoint has been
    *           injected, if null, certain validations aren't available
    * @param beanManager
    */
   public void validateInjectionPoint(InjectionPoint ij, BeanManagerImpl beanManager)
   {
      if (ij.getAnnotated().getAnnotation(New.class) != null && ij.getQualifiers().size() > 1)
      {
         throw new DefinitionException(NEW_WITH_QUALIFIERS, ij);
      }
      if (ij.getType().equals(InjectionPoint.class) && ij.getBean() == null)
      {
         throw new DefinitionException(INJECTION_INTO_NON_BEAN, ij);
      }
      if (ij.getType().equals(InjectionPoint.class) && !Dependent.class.equals(ij.getBean().getScope()))
      {
         throw new DefinitionException(INJECTION_INTO_NON_DEPENDENT_BEAN, ij);
      }
      if (ij.getType() instanceof TypeVariable<?>)
      {
         throw new DefinitionException(INJECTION_POINT_WITH_TYPE_VARIABLE, ij);
      }
      checkFacadeInjectionPoint(ij, Instance.class);
      checkFacadeInjectionPoint(ij, Event.class);
      Annotation[] bindings = ij.getQualifiers().toArray(new Annotation[0]);
      WeldAnnotated<?, ?> annotatedItem = ResolvableWeldClass.of(ij.getType(), bindings, beanManager);
      Set<?> resolvedBeans = beanManager.getBeanResolver().resolve(beanManager.getInjectableBeans(ij));
      if (resolvedBeans.isEmpty())
      {
         throw new DeploymentException(INJECTION_POINT_HAS_UNSATISFIED_DEPENDENCIES, ij, Arrays.toString(bindings));
      }
      if (resolvedBeans.size() > 1)
      {
         throw new DeploymentException(INJECTION_POINT_HAS_AMBIGUOUS_DEPENDENCIES, ij, Arrays.toString(bindings) + "; Possible dependencies: " + resolvedBeans);
      }
      Bean<?> resolvedBean = (Bean<?>) resolvedBeans.iterator().next();
      if (beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(resolvedBean.getScope()).isNormal() && !Proxies.isTypeProxyable(ij.getType()))
      {
         throw new UnproxyableResolutionException(INJECTION_POINT_HAS_NON_PROXYABLE_DEPENDENCIES, ij);
      }
      if (annotatedItem.isPrimitive() && resolvedBean.isNullable())
      {
         throw new NullableDependencyException(INJECTION_POINT_HAS_NULLABLE_DEPENDENCIES, ij);
      }
      if (ij.getBean() != null && Beans.isPassivatingScope(ij.getBean(), beanManager) && (!ij.isTransient()) && !Beans.isPassivationCapableBean(resolvedBean))
      {
         validateInjectionPointPassivationCapable(ij, resolvedBean, beanManager);
      }
   }

   public void validateInjectionPointPassivationCapable(InjectionPoint ij, Bean<?> resolvedBean, BeanManagerImpl beanManager)
   {
      if (!ij.isTransient() && !Beans.isPassivationCapableDependency(resolvedBean))
      {
         if (resolvedBean.getScope().equals(Dependent.class) && resolvedBean instanceof AbstractProducerBean<?, ?, ?>)
         {
            throw new IllegalProductException(NON_SERIALIZABLE_BEAN_INJECTED_INTO_PASSIVATING_BEAN, ij.getBean(), resolvedBean);
         }
         throw new UnserializableDependencyException(INJECTION_POINT_HAS_NON_SERIALIZABLE_DEPENDENCY, ij.getBean(), resolvedBean);
      }
   }

   public void validateDeployment(BeanManagerImpl manager, BeanDeployerEnvironment environment)
   {
      validateBeans(manager.getDecorators(), new ArrayList<RIBean<?>>(), manager);
      validateBeans(manager.getBeans(), new ArrayList<RIBean<?>>(), manager);
      validateEnabledDecoratorClasses(manager);
      validateEnabledInterceptorClasses(manager);
      validateEnabledAlternatives(manager);
      validateDisposalMethods(environment);
      validateBeanNames(manager);
   }

   public void validateBeans(Collection<? extends Bean<?>> beans, Collection<RIBean<?>> specializedBeans, BeanManagerImpl manager)
   {
      for (Bean<?> bean : beans)
      {
         if (bean instanceof RIBean<?>)
         {
            validateRIBean((RIBean<?>) bean, manager, specializedBeans);
         }
         else
         {
            validateBean(bean, manager);
         }
      }
   }

   public void validateBeanNames(BeanManagerImpl beanManager)
   {
      SetMultimap<String, Bean<?>> namedAccessibleBeans = Multimaps.newSetMultimap(new HashMap<String, Collection<Bean<?>>>(), new Supplier<Set<Bean<?>>>()
      {

         public Set<Bean<?>> get()
         {
            return new HashSet<Bean<?>>();
         }

      });
      for (Bean<?> bean : beanManager.getAccessibleBeans())
      {
         if (bean.getName() != null)
         {
            namedAccessibleBeans.put(bean.getName(), bean);
         }
      }

      List<String> accessibleNamespaces = new ArrayList<String>();
      for (String namespace : beanManager.getAccessibleNamespaces())
      {
         accessibleNamespaces.add(namespace);
      }

      for (String name : namedAccessibleBeans.keySet())
      {
         Set<Bean<?>> resolvedBeans = beanManager.getBeanResolver().resolve(Beans.removeDisabledAndSpecializedBeans(namedAccessibleBeans.get(name), beanManager.getEnabledAlternativeClasses(), beanManager.getEnabledAlternativeStereotypes(), beanManager.getSpecializedBeans()));
         if (resolvedBeans.size() > 1)
         {
            throw new DeploymentException(AMBIGUOUS_EL_NAME, name, resolvedBeans);
         }
         if (accessibleNamespaces.contains(name))
         {
            throw new DeploymentException(BEAN_NAME_IS_PREFIX, name);
         }
      }
   }

   private void validateEnabledInterceptorClasses(BeanManagerImpl beanManager)
   {
      Set<Class<?>> interceptorBeanClasses = new HashSet<Class<?>>();
      for (Interceptor<?> interceptor : beanManager.getInterceptors())
      {
         interceptorBeanClasses.add(interceptor.getBeanClass());
      }
      for (Class<?> enabledInterceptorClass : beanManager.getEnabledInterceptorClasses())
      {
         if (beanManager.getEnabledInterceptorClasses().indexOf(enabledInterceptorClass) < beanManager.getEnabledInterceptorClasses().lastIndexOf(enabledInterceptorClass))
         {
            throw new DeploymentException(INTERCEPTOR_SPECIFIED_TWICE, enabledInterceptorClass + " specified twice");
         }
         if (!interceptorBeanClasses.contains(enabledInterceptorClass))
         {
            throw new DeploymentException(INTERCEPTOR_NOT_ANNOTATED_OR_REGISTERED, enabledInterceptorClass);
         }
      }
   }

   private void validateEnabledDecoratorClasses(BeanManagerImpl beanManager)
   {
      // TODO Move building this list to the boot or sth
      Set<Class<?>> decoratorBeanClasses = new HashSet<Class<?>>();
      for (Decorator<?> bean : beanManager.getDecorators())
      {
         decoratorBeanClasses.add(bean.getBeanClass());
      }
      for (Class<?> clazz : beanManager.getEnabledDecoratorClasses())
      {
         if (beanManager.getEnabledDecoratorClasses().indexOf(clazz) < beanManager.getEnabledDecoratorClasses().lastIndexOf(clazz))
         {
            throw new DeploymentException(DECORATOR_SPECIFIED_TWICE, clazz);
         }
         if (!decoratorBeanClasses.contains(clazz))
         {
            throw new DeploymentException(DECORATOR_CLASS_NOT_BEAN_CLASS_OF_DECORATOR, clazz, decoratorBeanClasses);
         }
      }
   }

   private void validateEnabledAlternatives(BeanManagerImpl beanManager)
   {
      List<Class<?>> seenAlternatives = new ArrayList<Class<?>>();
      for (Class<? extends Annotation> stereotype : beanManager.getEnabledAlternativeStereotypes())
      {
         if (!stereotype.isAnnotationPresent(Alternative.class))
         {
            throw new DeploymentException(ALTERNATIVE_STEREOTYPE_NOT_ANNOTATED, stereotype);
         }
         if (seenAlternatives.contains(stereotype))
         {
            throw new DeploymentException(ALTERNATIVE_STEREOTYPE_SPECIFIED_MULTIPLE_TIMES, stereotype);
         }
         seenAlternatives.add(stereotype);
      }
      for (Class<?> clazz : beanManager.getEnabledAlternativeClasses())
      {
         if (!clazz.isAnnotationPresent(Alternative.class))
         {
            throw new DeploymentException(ALTERNATIVE_BEAN_CLASS_NOT_ANNOTATED, clazz);
         }
         if (seenAlternatives.contains(clazz))
         {
            throw new DeploymentException(ALTERNATIVE_BEAN_CLASS_SPECIFIED_MULTIPLE_TIMES, clazz);
         }
         seenAlternatives.add(clazz);
      }
   }

   private void validateDisposalMethods(BeanDeployerEnvironment environment)
   {
      Set<DisposalMethod<?, ?>> beans = environment.getUnresolvedDisposalBeans();
      if (!beans.isEmpty())
      {
         throw new DefinitionException(DISPOSAL_METHODS_WITHOUT_PRODUCER, beans);
      }
   }

   private static void checkFacadeInjectionPoint(InjectionPoint injectionPoint, Class<?> type)
   {
      if (injectionPoint.getAnnotated().getBaseType().equals(type))
      {
         if (injectionPoint.getType() instanceof ParameterizedType)
         {
            ParameterizedType parameterizedType = (ParameterizedType) injectionPoint.getType();
            if (parameterizedType.getActualTypeArguments()[0] instanceof TypeVariable<?>)
            {
               throw new DefinitionException(INJECTION_POINT_WITH_TYPE_VARIABLE, injectionPoint);
            }
            if (parameterizedType.getActualTypeArguments()[0] instanceof WildcardType)
            {
               throw new DefinitionException(INJECTION_POINT_HAS_WILDCARD, type, injectionPoint);
            }
         }
         else
         {
            throw new DefinitionException(INJECTION_POINT_MUST_HAVE_TYPE_PARAMETER, type, injectionPoint);
         }
      }

   }

   public void cleanup()
   {
   }

}
