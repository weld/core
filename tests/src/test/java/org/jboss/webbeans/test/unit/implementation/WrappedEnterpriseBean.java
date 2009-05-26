package org.jboss.webbeans.test.unit.implementation;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Named;

@SessionScoped
@Stateful
@Named("John")
class WrappedEnterpriseBean implements WrappedEnterpriseBeanLocal
{
   @Remove
   public void bye() {
   }
}
