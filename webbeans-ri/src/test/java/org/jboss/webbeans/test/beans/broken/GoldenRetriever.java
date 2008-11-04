package org.jboss.webbeans.test.beans.broken;

import javax.ejb.Stateful;
import javax.webbeans.Destructor;

@Stateful
public class GoldenRetriever
{
   @Destructor
   public void destroy() {
      
   }
}
