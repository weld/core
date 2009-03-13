package org.jboss.webbeans.test.unit.implementation;

import javax.annotation.Named;
import javax.context.SessionScoped;
import javax.ejb.Remove;
import javax.ejb.Stateful;

@SessionScoped
@Stateful
@Named("John")
class WrappedEnterpriseBean implements WrappedEnterpriseBeanLocal
{
   @Remove
   public void bye() {
   }
}
