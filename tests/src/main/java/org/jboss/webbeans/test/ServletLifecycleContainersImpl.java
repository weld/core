package org.jboss.webbeans.test;

import org.jboss.webbeans.mock.MockServletLifecycle;

public class ServletLifecycleContainersImpl extends AbstractStandaloneContainersImpl
{
   
   @Override
   protected MockServletLifecycle newLifecycle()
   {
      return new MockServletLifecycle();
   }
   
}
