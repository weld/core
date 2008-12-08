package org.jboss.webbeans.test.ejb.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Destructor;
import javax.webbeans.Disposes;
import javax.webbeans.RequestScoped;

@Stateful
@RequestScoped
public class GoldenRetriever
{
   @Remove @Destructor
   public void bye(@Disposes Object something) {
      
   }
   
}
