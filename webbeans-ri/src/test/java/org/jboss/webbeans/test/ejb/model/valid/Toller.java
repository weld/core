package org.jboss.webbeans.test.ejb.model.valid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.ejb.model.EnterpriseBeanRemoveMethodTest;

@Stateful
@RequestScoped
public class Toller
{
   @Remove
   public void bye() {
      EnterpriseBeanRemoveMethodTest.visited = true;
   }
}
