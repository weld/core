package org.jboss.webbeans.test.harness;

import org.jboss.testharness.spi.StandaloneContainers;
import org.jboss.webbeans.mock.MockEELifecycle;
import org.jboss.webbeans.mock.MockServletLifecycle;

public class StandaloneContainersImpl extends AbstractStandaloneContainersImpl implements StandaloneContainers
{
   
   @Override
   protected MockServletLifecycle newLifecycle()
   {
      return new MockEELifecycle();
   }
   
}
