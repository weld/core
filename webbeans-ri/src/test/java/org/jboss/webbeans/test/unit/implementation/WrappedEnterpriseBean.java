package org.jboss.webbeans.test.unit.implementation;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Named;
import javax.webbeans.SessionScoped;

@SessionScoped
@Stateful
@Named("John")
class WrappedEnterpriseBean
{
   @Remove
   public void bye() {
   }
}
