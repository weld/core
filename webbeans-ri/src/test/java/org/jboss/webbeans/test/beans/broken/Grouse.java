package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Initializer;
import javax.webbeans.Observes;

import org.jboss.webbeans.test.beans.DangerCall;


public class Grouse
{
   
   @Initializer
   public void setName(String name, @Observes DangerCall dangerCall)
   {
      // No-op
   }
   
}
