package org.jboss.webbeans.test.ejb.valid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.ejb.EnterpriseBeanRemoveMethodTest;

@Stateful
@RequestScoped
public class Toller
{
   @Remove
   public void bye() {
      EnterpriseBeanRemoveMethodTest.visited = true;
   }
}
