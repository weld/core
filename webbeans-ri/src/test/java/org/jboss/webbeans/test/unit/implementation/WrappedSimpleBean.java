package org.jboss.webbeans.test.unit.implementation;

import java.io.Serializable;

import javax.webbeans.Named;
import javax.webbeans.SessionScoped;

@SessionScoped
@Named("Fred")
class WrappedSimpleBean implements Serializable
{
   public WrappedSimpleBean() {
      
   }
}
