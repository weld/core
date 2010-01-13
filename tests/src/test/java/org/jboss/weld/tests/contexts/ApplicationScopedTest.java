package org.jboss.weld.tests.contexts;

import java.util.concurrent.CountDownLatch;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ApplicationScopedTest extends AbstractWeldTest
{

   @Test
   public void testConcurrentInitilized() throws InterruptedException
   {
      final CountDownLatch latch = new CountDownLatch(10);
      final ApplicationScopedObject applictionScopedObject = getReference(ApplicationScopedObject.class);
      for (int i = 0; i < 10; i++)
      {
         new Thread(new Runnable()
         {
            public void run()
            {
               try
               {
                  applictionScopedObject.increment();
               }
               finally
               {
                  latch.countDown();
               }
            }
         }).start();
      }
      latch.await();
      int value = applictionScopedObject.getValue();
      assert value == 10;
   }

}
