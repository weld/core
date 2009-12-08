package org.jboss.weld.tests.enterprise.proxyability;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

@SessionScoped
public class SimpleBean implements Serializable
{

   @Inject
   private MyStatelessBeanLocal myStatelessBean;

   public String getMessage()
   {
      return myStatelessBean.getText();
   }

}
