package org.jboss.webbeans.test.unit.implementation.newsimple;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Named;

@SessionScoped
@Named("Fred")
class WrappedSimpleBean implements Serializable
{
   public WrappedSimpleBean() {
      
   }
}
