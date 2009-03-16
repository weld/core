package org.jboss.webbeans.test.unit.implementation.enterprise.sbi;

import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.ejb.SessionBeanInterceptor;

public class MockSessionBeanInterceptor extends SessionBeanInterceptor
{
   
   @Override
   public EnterpriseBean<Object> getBean()
   {
      return super.getBean();
   }
   
}
