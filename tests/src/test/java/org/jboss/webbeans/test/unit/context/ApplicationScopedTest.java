package org.jboss.webbeans.test.unit.context;

import java.util.concurrent.CountDownLatch;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class ApplicationScopedTest extends AbstractWebBeansTest
{

   @Test
   public void testConcurrentInitilized() throws InterruptedException
   {
      final CountDownLatch latch = new CountDownLatch(10);
      for (int i = 0; i < 10; i++)
      {
         new Thread(new Runnable()
         {
            public void run()
            {
               try
               {
                  getCurrentManager().getInstanceByType(ApplictionScopedObject.class).increment();
               }
               finally
               {
                  latch.countDown();
               }
            }
         }).start();
      }
      latch.await();
      int value = getCurrentManager().getInstanceByType(ApplictionScopedObject.class).getValue();
      assert value == 10;
   }

}
