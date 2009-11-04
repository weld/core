package org.jboss.weld.tests.managed.newBean;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;

@SessionScoped
class WrappedSimpleBean implements Serializable
{
   public WrappedSimpleBean() {
      
   }
}
