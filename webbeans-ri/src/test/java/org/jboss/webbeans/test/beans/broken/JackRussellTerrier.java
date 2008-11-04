package org.jboss.webbeans.test.beans.broken;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Initializer;

@Stateful
public class JackRussellTerrier
{
   @Remove
   @Initializer
   public void destroy() {
      
   }
}
