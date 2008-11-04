package org.jboss.webbeans.test.beans.broken;

import javax.ejb.Remove;
import javax.ejb.Stateful;

@Stateful
public class Armant
{
   @Remove
   public void destroy() {
      
   }
}
