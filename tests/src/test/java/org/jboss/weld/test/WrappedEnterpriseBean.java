package org.jboss.weld.test;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;

@SessionScoped
@Stateful
class WrappedEnterpriseBean implements WrappedEnterpriseBeanLocal
{
   @Remove
   public void bye() {
   }
}
