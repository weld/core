package org.jboss.webbeans.test.unit.bootstrap;

import org.jboss.webbeans.mock.MockEELifecycle;
import org.testng.annotations.Test;

public class DiscoverFailsBootstrapTest
{
   
   @Test(groups="bootstrap", expectedExceptions=IllegalArgumentException.class)
   public void testDiscoverFails()
   {
      MockEELifecycle lifecycle = new MockEELifecycle();
      lifecycle.getBootstrap().startContainer(lifecycle.getEnvironment(), null, lifecycle.getApplicationBeanStore());
      lifecycle.initialize();
      lifecycle.beginApplication();
   }
   
}
