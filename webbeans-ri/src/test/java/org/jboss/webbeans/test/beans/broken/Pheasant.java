package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Initializer;
import javax.webbeans.Produces;

public class Pheasant
{
 
   @Initializer
   @Produces
   public void setName(String name)
   {
      // No-op
   }
   
}
