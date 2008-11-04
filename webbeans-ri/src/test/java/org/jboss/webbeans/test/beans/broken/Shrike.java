package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Destructor;
import javax.webbeans.Initializer;

public class Shrike
{
 
   
   @Initializer
   @Destructor
   public void setName(String name)
   {
      // No-op
   }
   
}
