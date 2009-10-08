package org.jboss.weld.test.harness;

import org.jboss.testharness.spi.StandaloneContainers;
import org.jboss.weld.mock.MockEELifecycle;
import org.jboss.weld.mock.MockServletLifecycle;

public class StandaloneContainersImpl extends AbstractStandaloneContainersImpl implements StandaloneContainers
{
   
   @Override
   protected MockServletLifecycle newLifecycle()
   {
      return new MockEELifecycle();
   }
   
}
