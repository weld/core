package org.jboss.weld.test.harness;

import org.jboss.weld.mock.MockServletLifecycle;

public class ServletLifecycleContainersImpl extends AbstractStandaloneContainersImpl
{
   
   @Override
   protected MockServletLifecycle newLifecycle()
   {
      return new MockServletLifecycle();
   }
   
}
