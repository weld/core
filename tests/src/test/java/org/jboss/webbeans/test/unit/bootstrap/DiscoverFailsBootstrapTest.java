package org.jboss.webbeans.test.unit.bootstrap;

import org.jboss.webbeans.bootstrap.spi.Deployment;
import org.jboss.webbeans.mock.MockEELifecycle;
import org.testng.annotations.Test;

public class DiscoverFailsBootstrapTest
{
   
   @Test(groups="bootstrap", expectedExceptions=IllegalStateException.class)
   public void testDiscoverFails()
   {
      MockEELifecycle lifecycle = new MockEELifecycle();
      lifecycle.getBootstrap().getServices().add(Deployment.class, null);
      lifecycle.initialize();
      lifecycle.beginApplication();
   }
   
}
