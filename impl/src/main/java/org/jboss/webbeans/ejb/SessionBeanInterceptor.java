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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.spi.CreationalContext;
import javax.interceptor.InvocationContext;

import org.jboss.webbeans.ContextualIdStore;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.proxy.EnterpriseBeanInstance;
import org.jboss.webbeans.bean.proxy.EnterpriseBeanProxyMethodHandler;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;

/**
 * Interceptor for handling EJB post-construct tasks
 * 
 * @author Pete Muir
 */
public class SessionBeanInterceptor implements Serializable
{
   private static final long serialVersionUID = 7327757031821596782L;

   private transient Log log = Logging.getLog(SessionBeanInterceptor.class);
   
   private transient EnterpriseBean<Object> bean;
   private transient CreationalContext<Object> creationalContext;
   
   private Integer beanId;
   private boolean contextual;
   
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
      initBean(target.getClass());
      bean.postConstruct(target);
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
      initBean(target.getClass());
      if (contextual)
      {
         bean.preDestroy(creationalContext);
         EnterpriseBeanInstance instance = getEnterpriseBeanInstance(bean);
         if (instance != null)
         {
            instance.setDestroyed(true);
         }
      }
      invocationContext.proceed();
   }

   /**
    * Gets a bean based on the target in the invocation context
    * 
    * @param invocationContext The invocation context
    * @return The found bean or null if the bean was not an enterprise bean
    */
   private void initBean(Class<? extends Object> beanClass)
   {
      EnterpriseBean<Object> bean = (EnterpriseBean<Object>) EnterpriseBeanProxyMethodHandler.getEnterpriseBean();
      if (bean != null && bean.getType().equals(beanClass))
      {
         this.bean = bean;
         this.contextual = true;
         this.creationalContext = (CreationalContext<Object>) EnterpriseBeanProxyMethodHandler.getEnterpriseBeanCreationalContext();
      }
      else
      {
         this.bean = (EnterpriseBean<Object>) CurrentManager.rootManager().getNewEnterpriseBeanMap().get(beanClass);
         this.contextual = false;
      }
      this.beanId = CurrentManager.rootManager().getServices().get(ContextualIdStore.class).getId(this.bean);
   }
   
   private static <T> EnterpriseBeanInstance getEnterpriseBeanInstance(EnterpriseBean<T> bean)
   {
      T instance = CurrentManager.rootManager().getContext(bean.getScopeType()).get(bean);
      if (instance instanceof EnterpriseBeanInstance)
      {
         return (EnterpriseBeanInstance) instance;
      }
      else if (instance == null)
      {
         return null;
      }
      else
      {
         throw new IllegalStateException("Contextual instance not an session bean created by the container");
      }
   }
   
   private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
   {
      ois.defaultReadObject();
      if (beanId != null)
      {
         bean = (EnterpriseBean<Object>) CurrentManager.rootManager().getServices().get(ContextualIdStore.class).getContextual(beanId);
      }
   }
   
   protected EnterpriseBean<Object> getBean()
   {
      return bean;
   }

}

   