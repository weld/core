package org.jboss.weld.tests.enterprise;

import javax.inject.Inject;

public class HelloAction
{
   
   public static long sleepDuration = 1000 * 2;
   
   @Inject
   private IHelloBean helloBean;

   private String hello;
   private String goodBye;

   public void executeRequest()
   {
      hello = helloBean.sayHello();
      try
      {
         Thread.sleep(sleepDuration);
      }
      catch (InterruptedException e)
      {
         System.out.println("Caught Interruption.");
      }
      goodBye = helloBean.sayGoodbye();
   }

   public String getHello()
   {
      return hello;
   }

   public String getGoodBye()
   {
      return goodBye;
   }

}
