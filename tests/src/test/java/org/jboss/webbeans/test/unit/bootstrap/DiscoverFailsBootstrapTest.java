package org.jboss.webbeans.test.unit.bootstrap;

import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.context.api.helpers.ConcurrentHashMapBeanStore;
import org.testng.annotations.Test;

public class DiscoverFailsBootstrapTest
{
   
   @Test(groups="bootstrap", expectedExceptions=IllegalArgumentException.class)
   public void testDiscoverFails()
   {
      Bootstrap bootstrap = new WebBeansBootstrap();
      bootstrap.startContainer(Environments.SE, null, new ConcurrentHashMapBeanStore());
   }
   
}
