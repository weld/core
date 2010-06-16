package org.jboss.weld.tests.ejb.singleton;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;

@Singleton
@Startup
@ApplicationScoped
public class Foo
{
   
   private static boolean postConstructCalled;
   
   public static boolean isPostConstructCalled()
   {
      return postConstructCalled;
   }
   
   public static void reset()
   {
      postConstructCalled = false;
   }
   
   @PostConstruct
   public void postConstruct()
   {
      postConstructCalled = true;
   }

}
