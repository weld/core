package org.jboss.webbeans.test.unit.bootstrap;

import org.jboss.webbeans.mock.MockLifecycle;
import org.testng.annotations.Test;

public class DiscoverFailsBootstrapTest
{
   
   @Test(groups="bootstrap", expectedExceptions=IllegalStateException.class)
   public void testDiscoverFails()
   {
      MockLifecycle lifecycle = new MockLifecycle(null);
      lifecycle.beginApplication();
   }
   
}
