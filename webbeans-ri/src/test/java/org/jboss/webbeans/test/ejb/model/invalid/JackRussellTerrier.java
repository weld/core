package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Destructor;
import javax.webbeans.Observes;
import javax.webbeans.RequestScoped;

@Stateful
@RequestScoped
public class JackRussellTerrier
{
   @Remove @Destructor
   public void bye(@Observes Object something) {
      
   }
   
}
