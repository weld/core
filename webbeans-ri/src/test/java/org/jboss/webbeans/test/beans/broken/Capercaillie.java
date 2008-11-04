package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Disposes;
import javax.webbeans.Initializer;

import org.jboss.webbeans.test.beans.ChickenHutch;

public class Capercaillie
{
   
   @Initializer
   public void setName(String name, @Disposes ChickenHutch chickenHutch)
   {
      // No-op
   }
   
}
