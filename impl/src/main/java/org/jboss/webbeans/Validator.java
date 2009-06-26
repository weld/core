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
package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.enterprise.inject.UnproxyableResolutionException;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.DecoratorBean;
import org.jboss.webbeans.bean.DisposalMethodBean;
import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.bean.NewSimpleBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.injection.resolution.ResolvableWBClass;
import org.jboss.webbeans.introspector.WBAnnotated;
import org.jboss.webbeans.metadata.MetaDataCache;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.ListComparator;
import org.jboss.webbeans.util.Names;
import org.jboss.webbeans.util.Proxies;
import org.jboss.webbeans.util.Reflections;

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
      boolean normalScoped = beanManager.getServices().get(MetaDataCache.class).getScopeModel(bean.getScopeType()).isNormal();
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
   private void validateRIBean(RIBean<?> bean, BeanManagerImpl beanManager, List<RIBean<?>> specializedBeans)
   {
      validateBean(bean, beanManager);
      if (!(bean instanceof NewSimpleBean) && !(bean instanceof NewEnterpriseBean))
      {
         RIBean<?> abstractBean = bean;
         if (abstractBean.isSpecializing())
         {
            if (!hasHigherPrecedence(bean.getDeploymentType(), abstractBean.getSpecializedBean().getDeploymentType(), beanManager))
            {
               throw new InconsistentSpecializationException("Specializing bean must have a higher precedence deployment type than the specialized bean: " + bean);
            }
            if (specializedBeans.contains(abstractBean.getSpecializedBean()))
            {
               throw new InconsistentSpecializationException("Two beans cannot specialize the same bean: " + bean);
            }
            specializedBeans.add(abstractBean.getSpecializedBean());
         }
         if (Beans.isPassivatingBean(bean, beanManager) && bean instanceof AbstractClassBean)
         {
            AbstractClassBean<?> classBean = (AbstractClassBean<?>) bean;
            if (classBean.hasDecorators())
            {
               for (Decorator<?> decorator : classBean.getDecorators())
               {
                  if (!decorator.isSerializable())
                  {
                     throw new UnserializableDependencyException("The bean " + bean + " declares a passivating scope but has non-serializable decorator: " + decorator); 
                  }
               }
            }
            
         }
      }

   }
   
   public void validateInjectionPoint(InjectionPoint ij, BeanManagerImpl beanManager)
   {
      if (ij.getAnnotated().getAnnotation(New.class) != null && ij.getBindings().size() > 1)
      {
         throw new DefinitionException("The injection point " + ij + " is annotated with @New which cannot be combined with other binding types");
      }
      if (ij.getType() instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType = (ParameterizedType) ij.getType();
         for (Type type : parameterizedType.getActualTypeArguments())
         {
            if (type instanceof TypeVariable)
            {
               throw new DefinitionException("Injection point cannot have a type variable type parameter " + ij);
            }
            if (type instanceof WildcardType)
            {
               throw new DefinitionException("Injection point cannot have a wildcard type parameter " + ij);
            }
         }
      }
      checkFacadeInjectionPoint(ij, Instance.class);
      checkFacadeInjectionPoint(ij, Event.class);
      Annotation[] bindings = ij.getBindings().toArray(new Annotation[0]);
      WBAnnotated<?, ?> annotatedItem = ResolvableWBClass.of(ij.getType(), bindings, beanManager);
      Set<?> resolvedBeans = beanManager.getInjectableBeans(ij);
      if (resolvedBeans.isEmpty())
      {
         throw new UnsatisfiedResolutionException("The injection point " + ij + " with binding types "  + Names.annotationsToString(ij.getBindings()) + " in " + ij.getBean() + " has unsatisfied dependencies with binding types ");
      }
      if (resolvedBeans.size() > 1)
      {
         throw new AmbiguousResolutionException("The injection point " + ij + " with binding types " + Names.annotationsToString(ij.getBindings()) + " in " + ij.getBean() + " has ambiguous dependencies");
      }
      Bean<?> resolvedBean = (Bean<?>) resolvedBeans.iterator().next();
      if (beanManager.getServices().get(MetaDataCache.class).getScopeModel(resolvedBean.getScopeType()).isNormal() && !Proxies.isTypeProxyable(ij.getType()))
      {
         throw new UnproxyableResolutionException("The injection point " + ij + " has non-proxyable dependencies");
      }
      if (Reflections.isPrimitive(annotatedItem.getJavaClass()) && resolvedBean.isNullable())
      {
         throw new NullableDependencyException("The injection point " + ij + " has nullable dependencies");
      }
      if (Beans.isPassivatingBean(ij.getBean(), beanManager) && !resolvedBean.isSerializable() && resolvedBean.getScopeType().equals(Dependent.class))
      {
         throw new UnserializableDependencyException("The bean " + ij.getBean() + " declares a passivating scope but has non-serializable dependency: " + resolvedBean);
      }
   }
   
   public void validateDeployment(BeanManagerImpl manager, BeanDeployerEnvironment environment)
   {
      List<RIBean<?>> specializedBeans = new ArrayList<RIBean<?>>();
      for (Bean<?> bean : manager.getBeans())
      {
         if (bean instanceof RIBean)
         {
            validateRIBean((RIBean<?>) bean, manager, specializedBeans);
         }
         else
         {
            validateBean(bean, manager);
         }
      }
      validateEnabledDecoratorClasses(manager);
      validateDisposalMethods(environment);
      
   }
   
   private void validateEnabledDecoratorClasses(BeanManagerImpl beanManager)
   {
      // TODO Move building this list to the boot or sth
      Set<Class<?>> decoratorBeanClasses = new HashSet<Class<?>>();
      for (DecoratorBean<?> bean : beanManager.getDecorators())
      {
         decoratorBeanClasses.add(bean.getType());
      }
      for (Class<?> clazz : beanManager.getEnabledDecoratorClasses())
      {
         if (!decoratorBeanClasses.contains(clazz))
         {
            throw new DeploymentException("Enabled decorator class " + clazz + " is not the bean class of at least one decorator bean (detected decorator beans " + decoratorBeanClasses + ")");
         }
      }
   }
   
   private void validateDisposalMethods(BeanDeployerEnvironment environment)
   {
      Set<DisposalMethodBean<?>> all = new HashSet<DisposalMethodBean<?>>(environment.getAllDisposalBeans());
      Set<DisposalMethodBean<?>> resolved = new HashSet<DisposalMethodBean<?>>(environment.getResolvedDisposalBeans());
      if (all.size() > 0 && !resolved.containsAll(all))
      {
         StringBuffer buff = new StringBuffer();
         buff.append("The following Disposal methods where not resolved\n");
         all.removeAll(resolved);
         for (DisposalMethodBean<?> bean : all)
         {
            buff.append(bean.toString());
         }
         throw new UnsatisfiedResolutionException(buff.toString());
      }
   }   

   private static boolean hasHigherPrecedence(Class<? extends Annotation> deploymentType, Class<? extends Annotation> otherDeploymentType, BeanManagerImpl manager)
   {
      Comparator<Class<? extends Annotation>> comparator = new ListComparator<Class<? extends Annotation>>(manager.getEnabledDeploymentTypes());
      return comparator.compare(deploymentType, otherDeploymentType) > 0;
   }
   
   private static void checkFacadeInjectionPoint(InjectionPoint injectionPoint, Class<?> type)
   {
      if (injectionPoint.getAnnotated().getBaseType().equals(type))
      {
         if (injectionPoint.getType() instanceof ParameterizedType)
         {
            ParameterizedType parameterizedType = (ParameterizedType) injectionPoint.getType();
            if (parameterizedType.getActualTypeArguments()[0] instanceof TypeVariable)
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

}
