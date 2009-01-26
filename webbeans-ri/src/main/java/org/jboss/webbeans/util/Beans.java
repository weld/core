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

import java.lang.reflect.Type;
import java.util.Set;

import javax.inject.manager.Bean;

import org.jboss.webbeans.MetaDataCache;
import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.EnterpriseBean;

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
   public static boolean isPassivatingBean(Bean<?> bean)
   {
      if (bean instanceof EnterpriseBean)
      {
         return ((EnterpriseBean<?>) bean).getEjbDescriptor().isStateful();
      }
      else
      {
         return MetaDataCache.instance().getScopeModel(bean.getScopeType()).isPassivating();
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
      if (bean instanceof AbstractBean)
      {
         return ((AbstractBean<?, ?>) bean).isProxyable();
      }
      else
      {
         return Beans.apiTypesAreProxyable(bean.getTypes());
      }
   }

   /**
    * Indicates if a set of types are all proxyable
    * 
    * @param types The types to test
    * @return True if proxyable, false otherwise
    */
   public static boolean apiTypesAreProxyable(Set<Type> types)
   {
      for (Type apiType : types)
      {
         if (Object.class.equals(apiType))
         {
            continue;
         }
         boolean isClass = !((Class<?>) apiType).isInterface();
         if (isClass && !Proxies.isClassProxyable((Class<?>) apiType))
         {
            return false;
         }
      }
      return true;
   }
   
}
