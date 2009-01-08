package org.jboss.webbeans.test.newbean.valid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Named;
import javax.webbeans.SessionScoped;

@SessionScoped
@Stateful
@Named("John")
public class WrappedEnterpriseBean
{
   @Remove
   public void bye() {
   }
}
