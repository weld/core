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

import java.util.HashSet;
import java.util.Set;

import javax.inject.BindingType;
import javax.inject.DefinitionException;
import javax.inject.Produces;
import javax.inject.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.injection.FieldInjectionPoint;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
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
   public static boolean isPassivatingBean(Bean<?> bean, ManagerImpl manager)
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
      return injectableFields;
   }
   
}
