package org.jboss.webbeans.test.mock;

import org.jboss.webbeans.context.RequestContext;

public class MockRequestContext extends RequestContext
{
   
   public MockRequestContext()
   {
      super();
      RequestContext.INSTANCE = this;
   }
   
}
