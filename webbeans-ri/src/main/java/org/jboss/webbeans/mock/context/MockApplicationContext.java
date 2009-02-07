package org.jboss.webbeans.mock.context;

import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.beanmap.SimpleBeanMap;

public class MockApplicationContext extends ApplicationContext
{
   
   public MockApplicationContext()
   {
      super();
      ApplicationContext.INSTANCE = this;
      setBeanMap(new SimpleBeanMap());
   }
   
}
