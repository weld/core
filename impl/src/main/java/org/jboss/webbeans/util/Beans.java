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
import java.util.HashSet;
import java.util.Set;

import javax.decorator.Decorates;
import javax.enterprise.inject.BindingType;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.inject.DefinitionException;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.injection.FieldInjectionPoint;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.metadata.BindingTypeModel;
import org.jboss.webbeans.metadata.MetaDataCache;

/**
 * Helper class for bean inspection
 * 
 * @author Pete Muir
 *
 */
public class Beans
{

   /**
    * Indicates if a bean is passivating
    * 
    * @param bean The bean to inspect
    * @return True if passivating, false otherwise
    */
   public static boolean isPassivatingBean(Bean<?> bean, BeanManagerImpl manager)
   {
      if (bean instanceof EnterpriseBean)
      {
         return ((EnterpriseBean<?>) bean).getEjbDescriptor().isStateful();
      }
      else
      {
         return manager.getServices().get(MetaDataCache.class).getScopeModel(bean.getScopeType()).isPassivating();
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
      if (bean instanceof RIBean)
      {
         return ((RIBean<?>) bean).isProxyable();
      }
      else
      {
         return Proxies.isTypesProxyable(bean.getTypes());
      }
   }

   public static Set<FieldInjectionPoint<?>> getFieldInjectionPoints(AnnotatedClass<?> annotatedItem, Bean<?> declaringBean)
   {
      Set<FieldInjectionPoint<?>> injectableFields = new HashSet<FieldInjectionPoint<?>>();
      for (AnnotatedField<?> annotatedField : annotatedItem.getMetaAnnotatedFields(BindingType.class))
      {
         addFieldInjectionPoint(annotatedField, injectableFields, declaringBean);
      }
      for (AnnotatedField<?> annotatedField : annotatedItem.getAnnotatedFields(Decorates.class))
      {
         addFieldInjectionPoint(annotatedField, injectableFields, declaringBean);
      }
      return injectableFields;
   }
   
   private static void addFieldInjectionPoint(AnnotatedField<?> annotatedField, Set<FieldInjectionPoint<?>> injectableFields, Bean<?> declaringBean)
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
         FieldInjectionPoint<?> fieldInjectionPoint = FieldInjectionPoint.of(declaringBean, annotatedField);
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
         BindingTypeModel<?> bindingType = manager.getServices().get(MetaDataCache.class).getBindingTypeModel(binding.annotationType());
         if (bindingType.getNonBindingTypes().size() > 0)
         {
            boolean matchFound = false;
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
         else if (!bindings2.contains(binding))
         {
            return false;
         }
      }
      return true;
   }
   
}
