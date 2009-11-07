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
package org.jboss.weld;

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
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.IllegalProductException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.enterprise.inject.UnproxyableResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.interceptor.model.InterceptionModel;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.NewManagedBean;
import org.jboss.weld.bean.NewSessionBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resolution.ResolvableWeldClass;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.Reflections;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Checks a list of beans for DeploymentExceptions and their subclasses
 * 
 * @author Nicklas Karlsson
 * 
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
         throw new UnproxyableResolutionException("Normal scoped bean " + bean + " is not proxyable");
      }
   }
   
   /**
    * Validate an RIBean.
    * 
    * This includes validating whether two beans specialize the same bean
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
               throw new InconsistentSpecializationException("Two beans cannot specialize the same bean: " + bean);
            }
            specializedBeans.add(abstractBean.getSpecializedBean());
         }
         if (Beans.isPassivationCapableBean(bean) && bean instanceof AbstractClassBean<?>)
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

   private void validateDirectlyDefinedInterceptorClasses(BeanManagerImpl beanManager, AbstractClassBean<?> classBean)
   {
      InterceptionModel<Class<?>, Class<?>> ejbInterceptorModel = beanManager.getClassDeclaredInterceptorsRegistry().getInterceptionModel(((AbstractClassBean<?>) classBean).getType());
      if (ejbInterceptorModel != null)
      {
         Class<?>[] classDeclaredInterceptors = ejbInterceptorModel.getAllInterceptors().toArray(new Class<?>[ejbInterceptorModel.getAllInterceptors().size()]);
         if (classDeclaredInterceptors != null)
         {
            for (Class<?> interceptorClass: classDeclaredInterceptors)
            {
               if (!Reflections.isSerializable(interceptorClass))
               {
                  throw new DeploymentException("The bean " + this + " declared a passivating scope, " +
                        "but has a non-serializable interceptor class: " + interceptorClass.getName());
               }
               InjectionTarget<Object> injectionTarget = (InjectionTarget<Object>) beanManager.createInjectionTarget(beanManager.createAnnotatedType(interceptorClass));
               for (InjectionPoint injectionPoint: injectionTarget.getInjectionPoints())
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
      InterceptionModel<Class<?>, SerializableContextual<Interceptor<?>, ?>> cdiInterceptorModel = beanManager.getCdiInterceptorsRegistry().getInterceptionModel(((AbstractClassBean<?>) classBean).getType());
      if (cdiInterceptorModel != null)
            {
               Collection<SerializableContextual<Interceptor<?>, ?>> interceptors = cdiInterceptorModel.getAllInterceptors();
               if (interceptors.size() > 0)
               {
                  for (SerializableContextual<Interceptor<?>, ?> serializableContextual : interceptors)
                  {
                     if (!Reflections.isSerializable(serializableContextual.get().getBeanClass()))
                     {
                        throw new DeploymentException("The bean " + this + " declared a passivating scope " +
                              "but has a non-serializable interceptor: "  + serializableContextual.get());
                     }
                     for (InjectionPoint injectionPoint: serializableContextual.get().getInjectionPoints())
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
         if (!Reflections.isSerializable(decorator.getBeanClass()))
         {
            throw new UnserializableDependencyException("The bean " + classBean + " declares a passivating scope but has non-serializable decorator: " + decorator);
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
    * @param declaringBean the bean into which the injectionPoint has been injected, if null, certain validations aren't available
    * @param beanManager
    */
   public void validateInjectionPoint(InjectionPoint ij, BeanManagerImpl beanManager)
   {
      if (ij.getAnnotated().getAnnotation(New.class) != null && ij.getQualifiers().size() > 1)
      {
         throw new DefinitionException("The injection point " + ij + " is annotated with @New which cannot be combined with other binding types");
      }
      if (ij.getType().equals(InjectionPoint.class) && ij.getBean() == null)
      {
         throw new DefinitionException("Cannot inject an Injection point into a class which isn't a bean " + ij);
      }
      if (ij.getType().equals(InjectionPoint.class) && !Dependent.class.equals(ij.getBean().getScope()))
      {
         throw new DefinitionException("Cannot inject an InjectionPoint into a non @Dependent scoped bean " + ij); 
      }
      if (ij.getType() instanceof TypeVariable<?>)
      {
         throw new DefinitionException("Cannot declare an injection point with a type variable " + ij);
      }
      checkFacadeInjectionPoint(ij, Instance.class);
      checkFacadeInjectionPoint(ij, Event.class);
      Annotation[] bindings = ij.getQualifiers().toArray(new Annotation[0]);
      WeldAnnotated<?, ?> annotatedItem = ResolvableWeldClass.of(ij.getType(), bindings, beanManager);
      Set<?> resolvedBeans = beanManager.getBeanResolver().resolve(beanManager.getInjectableBeans(ij));
      if (resolvedBeans.isEmpty())
      {
         throw new DeploymentException("Injection point has unstatisfied dependencies. Injection point: " + ij.toString() + "; Qualifiers: " + Arrays.toString(bindings));
      }
      if (resolvedBeans.size() > 1)
      {
         throw new DeploymentException("Injection point has ambiguous dependencies. Injection point: " + ij.toString() + "; Qualifiers: " + Arrays.toString(bindings) +"; Possible dependencies: " + resolvedBeans);
      }
      Bean<?> resolvedBean = (Bean<?>) resolvedBeans.iterator().next();
      if (beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(resolvedBean.getScope()).isNormal() && !Proxies.isTypeProxyable(ij.getType()))
      {
         throw new UnproxyableResolutionException("The injection point " + ij + " has non-proxyable dependencies");
      }
      if (Reflections.isPrimitive(annotatedItem.getJavaClass()) && resolvedBean.isNullable())
      {
         throw new NullableDependencyException("The injection point " + ij + " has nullable dependencies");
      }
      if (ij.getBean() != null && Beans.isPassivatingScope(ij.getBean(), beanManager) && (!ij.isTransient()) && !Beans.isPassivationCapableBean(resolvedBean))
      {
         validateInjectionPointPassivationCapable(ij, resolvedBean, beanManager);
      }
   }
   
   public void validateInjectionPointPassivationCapable(InjectionPoint ij, Bean<?> resolvedBean, BeanManagerImpl beanManager)
   {
      if (!ij.isTransient() && !Beans.isPassivationCapableBean(resolvedBean))
      {
         if (resolvedBean.getScope().equals(Dependent.class) && resolvedBean instanceof AbstractProducerBean<?, ?,?>)
         {
            throw new IllegalProductException("The bean " + ij.getBean() + " declares a passivating scope but the producer returned a non-serializable bean for injection: " + resolvedBean);
         }
         throw new UnserializableDependencyException("The bean " + ij.getBean() + " declares a passivating scope but has non-serializable dependency: " + resolvedBean);
      }
   }

   public void validateDeployment(BeanManagerImpl manager, BeanDeployerEnvironment environment)
   {
      validateBeans(manager.getDecorators(), new ArrayList<RIBean<?>>(), manager);
      validateBeans(manager.getBeans(), new ArrayList<RIBean<?>>(), manager);
      validateEnabledDecoratorClasses(manager);
      validateEnabledInterceptorClasses(manager);
      validateEnabledPolicies(manager);
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
      Multimap<String, Bean<?>> namedAccessibleBeans = Multimaps.newSetMultimap(new HashMap<String, Collection<Bean<?>>>(), new Supplier<Set<Bean<?>>>()
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
         Set<Bean<?>> resolvedBeans = beanManager.getBeanResolver().resolve(namedAccessibleBeans.get(name));
         if (resolvedBeans.size() > 1)
         {
            throw new DeploymentException("An unresolvable ambiguous EL name exists for " + name + "; found " + resolvedBeans );
         }
         if (accessibleNamespaces.contains(name))
         {
            throw new DeploymentException("The bean name " + name + " is used as a prefix for another bean");
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
      for (Class<?> enabledInterceptorClass: beanManager.getEnabledInterceptorClasses())
      {
         if (beanManager.getEnabledInterceptorClasses().indexOf(enabledInterceptorClass)
               < beanManager.getEnabledInterceptorClasses().lastIndexOf(enabledInterceptorClass))
         {
            throw new DeploymentException("Enabled interceptor class" + enabledInterceptorClass + " specified twice");
         }
         if (!interceptorBeanClasses.contains(enabledInterceptorClass))
         {
            throw new DeploymentException("Enabled interceptor class " + enabledInterceptorClass
                  + " is neither annotated with @Interceptor, nor registered through a portable extension");
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
            throw new DeploymentException("Enabled decorator class" + clazz + " specified twice");
         }
         if (!decoratorBeanClasses.contains(clazz))
         {
            throw new DeploymentException("Enabled decorator class " + clazz + " is not the bean class of at least one decorator bean (detected decorator beans " + decoratorBeanClasses + ")");
         }
      }
   }


   private void validateEnabledPolicies(BeanManagerImpl beanManager)
   {
      List<Class<?>> seenPolicies = new ArrayList<Class<?>>();
      for (Class<? extends Annotation> stereotype : beanManager.getEnabledPolicyStereotypes())
      {
         if (!stereotype.isAnnotationPresent(Alternative.class))
         {
            throw new DeploymentException("Enabled policy sterotype " + stereotype + " is not annotated @Policy");
         }
         if (seenPolicies.contains(stereotype))
         {
            throw new DeploymentException("Cannot enable the same policy sterotype " + stereotype + " in beans.xml");
         }
         seenPolicies.add(stereotype);
      }
      for (Class<?> clazz : beanManager.getEnabledPolicyClasses())
      {
         if (!clazz.isAnnotationPresent(Alternative.class))
         {
            throw new DeploymentException("Enabled policy bean class " + clazz + " is not annotated @Policy");
         }
         if (seenPolicies.contains(clazz))
         {
            throw new DeploymentException("Cannot enable the same policy bean class " + clazz + " in beans.xml");
         }
         seenPolicies.add(clazz);
      }
   }
   
   private void validateDisposalMethods(BeanDeployerEnvironment environment)
   {
      Set<DisposalMethod<?, ?>> beans = environment.getUnresolvedDisposalBeans();
      if (!beans.isEmpty())
      {
         throw new DefinitionException("The following Disposal methods were declared but did not resolved to a producer method " + beans);
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
               throw new DefinitionException("An injection point of type " + type + " cannot have a type variable type parameter " + injectionPoint);
            }
            if (parameterizedType.getActualTypeArguments()[0] instanceof WildcardType)
            {
               throw new DefinitionException("An injection point of type " + type + " cannot have a wildcard type parameter " + injectionPoint);
            }
         }
         else
         {
            throw new DefinitionException("An injection point of type " + type + " must have a type parameter " + injectionPoint);
         }
      }
      
   }


   public void cleanup() {}

}
