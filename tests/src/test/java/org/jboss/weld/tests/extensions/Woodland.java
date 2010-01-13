package org.jboss.weld.tests.extensions;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;

public class Woodland
{
   
   private static boolean postConstructCalled;
   
   @Produces
   private Capercaillie capercaillie = new Capercaillie("bob");
   
   @PostConstruct
   public void postConstruct()
   {
      postConstructCalled = true;
   }
   
   public static boolean isPostConstructCalled()
   {
      return postConstructCalled;
   }
   
   public static void reset()
   {
      postConstructCalled = false;
   }

}
