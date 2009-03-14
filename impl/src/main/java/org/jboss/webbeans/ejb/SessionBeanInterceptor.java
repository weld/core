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

package org.jboss.webbeans.ejb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.InvocationContext;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.proxy.EnterpriseBeanInstance;
import org.jboss.webbeans.bean.proxy.EnterpriseBeanProxyMethodHandler;

/**
 * Interceptor for handling EJB post-construct tasks
 * 
 * @author Pete Muir
 */
public class SessionBeanInterceptor
{
   /**
    * Gets the underlying target and calls the post-construct method
    * 
    * @param invocationContext The invocation context
    * @throws Exception 
    */
   @PostConstruct
   public void postConstruct(InvocationContext invocationContext) throws Exception
   {
      Object target = invocationContext.getTarget();
      EnterpriseBean<Object> enterpriseBean = getBean(target.getClass());
      if (enterpriseBean != null)
      {
         enterpriseBean.postConstruct(target);
      }
      invocationContext.proceed();
   }

   /**
    * Gets the underlying target and calls the pre-destroy method
    * 
    * @param invocationContext The invocation context
    * @throws Exception 
    */
   @PreDestroy
   public void preDestroy(InvocationContext invocationContext) throws Exception
   {
      Object target = invocationContext.getTarget();
      EnterpriseBean<Object> enterpriseBean = getBean(target.getClass());
      if (enterpriseBean != null)
      {
         enterpriseBean.preDestroy(target);
      }
      getEnterpriseBeanInstance(enterpriseBean).setDestroyed(true);
      invocationContext.proceed();
   }

   /**
    * Gets a bean based on the target in the invocation context
    * 
    * @param invocationContext The invocation context
    * @return The found bean or null if the bean was not an enterprise bean
    */
   private static <T> EnterpriseBean<T> getBean(Class<? extends T> beanClass)
   {
      if (EnterpriseBeanProxyMethodHandler.isContextualInstance(beanClass))
      {
         // Access all non-new enterprise beans. 
         // TODO Deal with XML defined enterprise beans!
         return (EnterpriseBean<T>) CurrentManager.rootManager().getEnterpriseBeanMap().get(beanClass);
      }
      else
      {
         // Access all @New enterprise beans
         return (EnterpriseBean<T>) CurrentManager.rootManager().getNewEnterpriseBeanMap().get(beanClass);
      }
   }
   
   private static <T> EnterpriseBeanInstance getEnterpriseBeanInstance(EnterpriseBean<T> bean)
   {
      T instance = CurrentManager.rootManager().getContext(bean.getScopeType()).get(bean);
      if (instance instanceof EnterpriseBeanInstance)
      {
         return (EnterpriseBeanInstance) instance;
      }
      else
      {
         throw new IllegalStateException("Contextual instance not an session bean created by the container");
      }
   }

}
