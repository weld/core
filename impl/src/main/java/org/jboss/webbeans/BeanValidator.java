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
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.context.Dependent;
import javax.event.Event;
import javax.event.Fires;
import javax.inject.AmbiguousDependencyException;
import javax.inject.DefinitionException;
import javax.inject.InconsistentSpecializationException;
import javax.inject.Instance;
import javax.inject.New;
import javax.inject.NullableDependencyException;
import javax.inject.Obtains;
import javax.inject.UnproxyableDependencyException;
import javax.inject.UnsatisfiedDependencyException;
import javax.inject.UnserializableDependencyException;
import javax.inject.manager.Bean;
import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.bean.NewSimpleBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.injection.resolution.ResolvableAnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedItem;
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
   @SuppressWarnings("unchecked")
   public void validate()
   {
      final List<Bean<?>> specializedBeans = new ArrayList<Bean<?>>();
      for (Bean<?> bean : manager.getBeans())
      {
         for (InjectionPoint injectionPoint : bean.getInjectionPoints())
         {
            if (injectionPoint.getAnnotation(New.class) != null && injectionPoint.getBindings().size() > 1)
            {
               throw new DefinitionException("The injection point " + injectionPoint + " is annotated with @New which cannot be combined with other binding types");
            }
            checkFacadeInjectionPoint(injectionPoint, Obtains.class, Instance.class);
            checkFacadeInjectionPoint(injectionPoint, Fires.class, Event.class);
            Annotation[] bindings = injectionPoint.getBindings().toArray(new Annotation[0]);
            AnnotatedItem<?, ?> annotatedItem = ResolvableAnnotatedClass.of(injectionPoint.getType(), bindings);
            Set<?> resolvedBeans = manager.resolveByType(annotatedItem, injectionPoint, bindings);
            if (resolvedBeans.isEmpty())
            {
               throw new UnsatisfiedDependencyException("The injection point " + injectionPoint + " with binding types "  + Names.annotationsToString(injectionPoint.getBindings()) + " in " + bean + " has unsatisfied dependencies with binding types ");
            }
            if (resolvedBeans.size() > 1)
            {
               throw new AmbiguousDependencyException("The injection point " + injectionPoint + " with binding types " + Names.annotationsToString(injectionPoint.getBindings()) + " in " + bean + " has ambiguous dependencies");
            }
            Bean<?> resolvedBean = (Bean<?>) resolvedBeans.iterator().next();
            if (MetaDataCache.instance().getScopeModel(resolvedBean.getScopeType()).isNormal() && !Proxies.isTypeProxyable(injectionPoint.getType()))
            {
               throw new UnproxyableDependencyException("The injection point " + injectionPoint + " has non-proxyable dependencies");
            }
            if (Reflections.isPrimitive(annotatedItem.getRawType()) && resolvedBean.isNullable())
            {
               throw new NullableDependencyException("The injection point " + injectionPoint + " has nullable dependencies");
            }
            if (Beans.isPassivatingBean(bean) && !resolvedBean.isSerializable() && resolvedBean.getScopeType().equals(Dependent.class))
            {
               throw new UnserializableDependencyException("The bean " + bean + " declares a passivating scopes but has non-serializable dependencies");
            }
         }
         if (bean instanceof RIBean && !(bean instanceof NewSimpleBean) && !(bean instanceof NewEnterpriseBean))
         {
            RIBean<?> abstractBean = (RIBean<?>) bean;
            if (abstractBean.isSpecializing())
            {
               if (!hasHigherPrecedence(bean.getDeploymentType(), abstractBean.getSpecializedBean().getDeploymentType()))
               {
                  throw new InconsistentSpecializationException("Specializing bean must have a higher precedence deployment type than the specialized bean: " + bean);
               }
               if (specializedBeans.contains(abstractBean.getSpecializedBean()))
               {
                  throw new InconsistentSpecializationException("Two beans cannot specialize the same bean: " + bean);
               }
               specializedBeans.add(abstractBean.getSpecializedBean());
            }
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
   
   private void checkFacadeInjectionPoint(InjectionPoint injectionPoint, Class<? extends Annotation> annotationType, Class<?> type)
   {
      if (injectionPoint.isAnnotationPresent(annotationType))
      {
         if (injectionPoint.getType() instanceof ParameterizedType)
         {
            ParameterizedType parameterizedType = (ParameterizedType) injectionPoint.getType();
            if (!type.isAssignableFrom((Class<?>) parameterizedType.getRawType()))
            {
               throw new DefinitionException("An injection point annotated " + annotationType + " must have type " + type + " " + injectionPoint);
            }
            if (parameterizedType.getActualTypeArguments()[0] instanceof TypeVariable)
            {
               throw new DefinitionException("An injection point annotated " + annotationType + " cannot have a type variable type parameter " + injectionPoint);
            }
            if (parameterizedType.getActualTypeArguments()[0] instanceof WildcardType)
            {
               throw new DefinitionException("An injection point annotated " + annotationType + " cannot have a wildcard type parameter " + injectionPoint);
            }
         }
         else
         {
            throw new DefinitionException("An injection point annotated " + annotationType + " must have a type parameter " + injectionPoint);
         }
      }
      
   }

}
