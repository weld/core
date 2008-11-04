package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Initializer;

public class Dottrel
{
   
   @Initializer
   public static void setName(String name)
   {
      // No-op
   }
   
}
