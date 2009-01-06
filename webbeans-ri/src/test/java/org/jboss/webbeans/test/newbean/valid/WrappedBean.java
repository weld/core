package org.jboss.webbeans.test.newbean.valid;

import java.io.Serializable;

import javax.webbeans.Named;
import javax.webbeans.SessionScoped;

@SessionScoped
@Named("Fred")
public class WrappedBean implements Serializable
{
   public WrappedBean() {
      
   }
}
