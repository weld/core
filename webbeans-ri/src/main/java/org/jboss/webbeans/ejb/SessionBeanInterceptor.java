package org.jboss.webbeans.ejb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.InvocationContext;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.bean.EnterpriseBean;

public class SessionBeanInterceptor
{
   
   @PostConstruct
   public void postConstruct(InvocationContext invocationContext)
   {
      EnterpriseBean<Object> enterpriseBean = getBean(invocationContext);
      if (enterpriseBean != null)
      {
         enterpriseBean.postConstruct(invocationContext.getTarget());
      }
   }
   
   @PreDestroy
   public void preDestroy(InvocationContext invocationContext)
   {
      EnterpriseBean<Object> enterpriseBean = getBean(invocationContext);
      if (enterpriseBean != null)
      {
         enterpriseBean.preDestroy(invocationContext.getTarget());
      }
   }
   
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
