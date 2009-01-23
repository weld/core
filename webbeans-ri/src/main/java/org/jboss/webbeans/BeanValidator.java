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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.webbeans.AmbiguousDependencyException;
import javax.webbeans.DefinitionException;
import javax.webbeans.InconsistentSpecializationException;
import javax.webbeans.InjectionPoint;
import javax.webbeans.New;
import javax.webbeans.NullableDependencyException;
import javax.webbeans.UnproxyableDependencyException;
import javax.webbeans.UnsatisfiedDependencyException;
import javax.webbeans.UnserializableDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.bean.NewSimpleBean;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.ListComparator;
import org.jboss.webbeans.util.Proxies;
import org.jboss.webbeans.util.Reflections;

/**
 * Checks a list of beans for DeploymentExceptions and their subclasses
 * 
 * @author Nicklas Karlsson
 *
 */
public class BeanValidator
{
   
   private final ManagerImpl manager;
   
   public BeanValidator(ManagerImpl manager)
   {
      this.manager = manager;
   }

   /**
    * Validates the beans
    * 
    * @param beans The beans to validate
    */
   public void validate(List<Bean<?>> beans)
   {
      final List<Bean<?>> specializedBeans = new ArrayList<Bean<?>>();
      for (Bean<?> bean : beans)
      {
         for (InjectionPoint injectionPoint : bean.getInjectionPoints())
         {
            if (injectionPoint.getType() instanceof Class)
            {
               Class<?> type = (Class<?>) injectionPoint.getType();
               if (injectionPoint.getAnnotation(New.class) != null && injectionPoint.getBindings().size() > 1)
               {
                  throw new DefinitionException("The injection point " + injectionPoint + " is annotated with @New which cannot be combined with other binding types");
               }
               Annotation[] bindings = injectionPoint.getBindings().toArray(new Annotation[0]);
               Set<?> resolvedBeans = manager.resolveByType(type, bindings);
               if (resolvedBeans.isEmpty())
               {
                  throw new UnsatisfiedDependencyException("The injection point " + injectionPoint + " has unsatisfied dependencies for type " + type + " and binding types " + bindings + " in " + bean);
               }
               if (resolvedBeans.size() > 1)
               {
                  throw new AmbiguousDependencyException("The injection point " + injectionPoint + " has ambiguos dependencies for type " + type + " and binding types " + bindings + " in " + bean);
               }
               Bean<?> resolvedBean = (Bean<?>) resolvedBeans.iterator().next();
               if (MetaDataCache.instance().getScopeModel(resolvedBean.getScopeType()).isNormal() && !Proxies.isClassProxyable(type))
               {
                  throw new UnproxyableDependencyException("The injection point " + injectionPoint + " has non-proxyable dependencies");
               }
               if (Reflections.isPrimitive((Class<?>) injectionPoint.getType()) && resolvedBean.isNullable())
               {
                  throw new NullableDependencyException("The injection point " + injectionPoint + " has nullable dependencies");
               }
            }
            else
            {
               throw new UnsupportedOperationException("Not yet implemented");
            }
         }
         if (bean instanceof AbstractBean && !(bean instanceof NewSimpleBean) && !(bean instanceof NewEnterpriseBean))
         {
            AbstractBean<?, ?> abstractBean = (AbstractBean<?, ?>) bean;
            if (abstractBean.isSpecializing())
            {
               if (!hasHigherPrecedence(bean.getDeploymentType(), abstractBean.getSpecializedBean().getDeploymentType()))
               {
                  throw new InconsistentSpecializationException("Specializing bean must have a higher precedence deployment type than the specialized bean");
               }
               if (specializedBeans.contains(abstractBean.getSpecializedBean()))
               {
                  throw new InconsistentSpecializationException("Two beans cannot specialize the same bean");
               }
               specializedBeans.add(abstractBean.getSpecializedBean());
            }
         } 
         if (Beans.isPassivatingBean(bean) && !bean.isSerializable())
         {
            throw new UnserializableDependencyException("The bean " + bean + " declares a passivating scopes but has non-serializable dependencies");
         }
         boolean normalScoped = MetaDataCache.instance().getScopeModel(bean.getScopeType()).isNormal();
         if (normalScoped && !Beans.isBeanProxyable(bean))
         {
            throw new UnproxyableDependencyException("Normal scoped bean " + bean + " is not proxyable");
         }
      }

   }
   
   
   private boolean hasHigherPrecedence(Class<? extends Annotation> deploymentType, Class<? extends Annotation> otherDeploymentType)
   {
      Comparator<Class<? extends Annotation>> comparator = new ListComparator<Class<? extends Annotation>>(manager.getEnabledDeploymentTypes());
      return comparator.compare(deploymentType, otherDeploymentType) > 0;
   }


}
