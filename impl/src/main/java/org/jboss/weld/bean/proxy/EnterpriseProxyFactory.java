/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.weld.bean.proxy;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtNewConstructor;

import org.jboss.weld.exceptions.WeldException;

/**
 * This factory produces proxies specific for enterprise beans, in particular
 * session beans.  It adds the interface {@link EnterpriseBeanInstance} to
 * each proxy class.
 * 
 * @author David Allen
 */
public class EnterpriseProxyFactory<T> extends ProxyFactory<T>
{
   private static final String PROXY_SUFFIX = "EnterpriseProxy";

   /**
    * Produces a factory for a specific bean implementation.
    * 
    * @param proxiedBeanType the actual enterprise bean
    */
   public EnterpriseProxyFactory(Class<T> proxiedBeanType)
   {
      super(proxiedBeanType);
      addInterface(EnterpriseBeanInstance.class);
   }

   // Not sure this is a useful approach, but non-default constructors are problematic (DRA)
   @Override
   protected void addConstructors(CtClass proxyClassType)
   {
      try
      {
         CtClass baseType = classPool.get(beanType.getName());
         for (CtConstructor constructor : baseType.getConstructors())
         {
            proxyClassType.addConstructor(CtNewConstructor.copy(constructor, proxyClassType, null));
         }
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   @Override
   protected String getProxyNameSuffix()
   {
      return PROXY_SUFFIX;
   }

}
