package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Stateful;
import javax.webbeans.Destructor;

@Stateful
public class GoldenRetriever
{
   @Destructor
   public void destroy() {
      
   }
}
