package org.jboss.webbeans.ejb;

import javax.annotation.PostConstruct;
import javax.interceptor.InvocationContext;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.bean.EnterpriseBean;

public class SessionBeanInterceptor
{
   
   @PostConstruct
   public void postConstruct(InvocationContext invocationContext)
   {
      Class<?> beanClass = invocationContext.getTarget().getClass();
      // TODO Don't like this
      Bean<?> bean = CurrentManager.rootManager().getBeanMap().get(beanClass);
      if (bean instanceof EnterpriseBean)
      {
         EnterpriseBean<Object> enterpriseBean = (EnterpriseBean<Object>) bean;
         enterpriseBean.postConstruct(invocationContext.getTarget());
      }
   }
   
}
