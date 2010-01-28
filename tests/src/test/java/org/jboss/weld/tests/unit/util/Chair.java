package org.jboss.weld.tests.unit.util;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

@Default
public class Chair
{
   @Produces
   public int legs;
   
   @Produces
   public String sit()
   {
      return "sitting";
   }
}
