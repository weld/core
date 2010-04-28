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
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;

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
   }

   @Override
   protected String getProxyNameSuffix()
   {
      return PROXY_SUFFIX;
   }

   @Override
   protected void addSpecialMethods(CtClass proxyClassType)
   {
      super.addSpecialMethods(proxyClassType);
      
      // Add methods for the EnterpriseBeanInstance interface
      try
      {
         CtClass enterpriseBeanInstanceInterface = classPool.get(EnterpriseBeanInstance.class.getName());
         proxyClassType.addInterface(enterpriseBeanInstanceInterface);
         for (CtMethod method : enterpriseBeanInstanceInterface.getDeclaredMethods())
         {
            log.trace("Adding method " + method.getLongName());
            proxyClassType.addMethod(CtNewMethod.make(method.getReturnType(), method.getName(), method.getParameterTypes(), method.getExceptionTypes(), createSpecialInterfaceBody(method, EnterpriseBeanInstance.class), proxyClassType));
         }
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
      
   }
}
