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
import java.util.List;
import java.util.Set;

import javax.webbeans.AmbiguousDependencyException;
import javax.webbeans.InjectionPoint;
import javax.webbeans.NullableDependencyException;
import javax.webbeans.UnproxyableDependencyException;
import javax.webbeans.UnsatisfiedDependencyException;
import javax.webbeans.UnserializableDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.MetaDataCache;

/**
 * Checks a list of beans for DeploymentExceptions and their subclasses
 * 
 * @author Nicklas Karlsson
 *
 */
public class BeanValidation
{

   /**
    * Validates the beans
    * 
    * @param beans The beans to validate
    */
   public static void validate(List<Bean<?>> beans)
   {
      for (Bean<?> bean : beans)
      {
         for (InjectionPoint injectionPoint : bean.getInjectionPoints())
         {
            Class<?> type = (Class<?>) injectionPoint.getType();
            Annotation[] bindingTypes = injectionPoint.getBindings().toArray(new Annotation[0]);
            Set<?> resolvedBeans = CurrentManager.rootManager().resolveByType(type, bindingTypes);
            if (resolvedBeans.isEmpty())
            {
               throw new UnsatisfiedDependencyException("The injection point " + injectionPoint + " has unsatisfied dependencies for type " + type + " and binding types " + bindingTypes + " in " + bean);
            }
            if (resolvedBeans.size() > 1)
            {
               throw new AmbiguousDependencyException("The injection point " + injectionPoint + " has ambiguos dependencies for type " + type + " and binding types " + bindingTypes + " in " + bean);
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
            // Specialization checks
         }
         if (Reflections.isPassivatingBean(bean) && !bean.isSerializable())
         {
            throw new UnserializableDependencyException("The bean " + bean + " declares a passivating scopes but has non-serializable dependencies");
         }
         boolean normalScoped = MetaDataCache.instance().getScopeModel(bean.getScopeType()).isNormal();
         if (normalScoped && !Proxies.isBeanProxyable(bean))
         {
            throw new UnproxyableDependencyException("Normal scoped bean " + bean + " is not proxyable");
         }
      }

   }

}
