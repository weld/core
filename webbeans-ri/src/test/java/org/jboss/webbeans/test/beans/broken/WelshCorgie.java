package org.jboss.webbeans.test.beans.broken;

import javax.ejb.Stateless;
import javax.webbeans.Destructor;

@Stateless
public class WelshCorgie
{
   @Destructor
   public void destroy() {
      
   }
}
