package org.jboss.webbeans.test.unit.implementation;

import java.io.Serializable;

import javax.annotation.Named;
import javax.context.SessionScoped;

@SessionScoped
@Named("Fred")
class WrappedSimpleBean implements Serializable
{
   public WrappedSimpleBean() {
      
   }
}
