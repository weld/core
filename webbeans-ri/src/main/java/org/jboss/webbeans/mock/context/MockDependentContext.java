package org.jboss.webbeans.mock.context;

import org.jboss.webbeans.context.DependentContext;

public class MockDependentContext extends DependentContext
{
   
   public MockDependentContext()
   {
      super();
      DependentContext.INSTANCE = this;
   }
   
}
