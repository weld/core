package org.jboss.webbeans.test.mock;

import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.context.beanmap.SimpleBeanMap;

public class MockSessionContext extends SessionContext
{
   
   public MockSessionContext()
   {
      super();
      SessionContext.INSTANCE = this;
      setBeanMap(new SimpleBeanMap());
   }
   
}
