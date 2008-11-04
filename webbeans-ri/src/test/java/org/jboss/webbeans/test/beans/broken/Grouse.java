package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Observes;

import org.jboss.webbeans.test.beans.DangerCall;


public class Grouse
{
   
   public void setName(String name, @Observes DangerCall dangerCall)
   {
      // No-op
   }
   
}
