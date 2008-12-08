package org.jboss.webbeans.test.ejb.valid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.ApplicationScoped;
import javax.webbeans.Destructor;
import javax.webbeans.Named;

import org.jboss.webbeans.test.ejb.EnterpriseBeanRemoveMethodTest;

@Stateful
@ApplicationScoped
@Named
public class Pitbull
{

   @Remove @Destructor
   public void bye() {
      EnterpriseBeanRemoveMethodTest.visited = true;
   }
}
