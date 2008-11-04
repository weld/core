package org.jboss.webbeans.test.beans.broken;

import javax.ejb.Stateful;
import javax.webbeans.Destructor;

@Stateful
public class Whippet
{
   @Destructor
   public void destroy1() {
      
   }
   
   @Destructor
   public void destroy2() {
      
   }
}
