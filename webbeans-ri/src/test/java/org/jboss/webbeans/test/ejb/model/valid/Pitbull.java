package org.jboss.webbeans.test.ejb.model.valid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.ApplicationScoped;
import javax.webbeans.Destructor;
import javax.webbeans.Named;

import org.jboss.webbeans.test.ejb.model.EnterpriseBeanRemoveMethodTest;

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
