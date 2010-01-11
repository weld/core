package org.jboss.weld.tests.unit.bootstrap;

import org.jboss.weld.Container;
import org.jboss.weld.ContainerState;
import org.jboss.weld.mock.MockServletLifecycle;
import org.jboss.weld.mock.TestContainer;
import org.testng.annotations.Test;

public class ContainerStatusTest
{
   
   @Test
   public void testStatus()
   {
      TestContainer container = new TestContainer(new MockServletLifecycle());
      assert !Container.available();
      container.getLifecycle().initialize();
      assert !Container.available();
      assert Container.instance().getState().equals(ContainerState.STARTING);
      container.getLifecycle().getBootstrap().startInitialization();
      assert !Container.available();
      assert Container.instance().getState().equals(ContainerState.STARTING);
      container.getLifecycle().getBootstrap().deployBeans();
      assert Container.available();
      assert Container.instance().getState().equals(ContainerState.INITIALIZED);
      container.getLifecycle().getBootstrap().validateBeans().endInitialization();
      assert Container.available();
      assert Container.instance().getState().equals(ContainerState.VALIDATED);
      container.stopContainer();
      assert !Container.available();
   }

}
