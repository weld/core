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
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.bean.EnterpriseBean;

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
    */
   @PostConstruct
   public void postConstruct(InvocationContext invocationContext)
   {
      EnterpriseBean<Object> enterpriseBean = getBean(invocationContext);
      if (enterpriseBean != null)
      {
         enterpriseBean.postConstruct(invocationContext.getTarget());
      }
   }

   /**
    * Gets the underlying target and calls the pre-destroy method
    * 
    * @param invocationContext The invocation context
    */
   @PreDestroy
   public void preDestroy(InvocationContext invocationContext)
   {
      EnterpriseBean<Object> enterpriseBean = getBean(invocationContext);
      if (enterpriseBean != null)
      {
         enterpriseBean.preDestroy(invocationContext.getTarget());
      }
   }

   /**
    * Gets a bean based on the target in the invocation context
    * 
    * @param invocationContext The invocation context
    * @return The found bean or null if the bean was not an enterprise bean
    */
   @SuppressWarnings("unchecked")
   private static EnterpriseBean<Object> getBean(InvocationContext invocationContext)
   {
      Class<?> beanClass = invocationContext.getTarget().getClass();
      Bean<?> bean = CurrentManager.rootManager().getBeanMap().get(beanClass);
      if (bean instanceof EnterpriseBean)
      {
         return (EnterpriseBean<Object>) bean;
      }
      else
      {
         return null;
      }
   }

}
