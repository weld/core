package org.jboss.weld.tests.unit.bootstrap;

import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.testng.annotations.Test;

public class DiscoverFailsBootstrapTest
{
   
   @Test(groups="bootstrap", expectedExceptions=IllegalArgumentException.class)
   public void testDiscoverFails()
   {
      Bootstrap bootstrap = new WeldBootstrap();
      bootstrap.startContainer(Environments.SE, null, new ConcurrentHashMapBeanStore());
   }
   
}
