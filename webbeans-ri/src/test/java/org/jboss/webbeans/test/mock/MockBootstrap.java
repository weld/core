package org.jboss.webbeans.test.mock;

import java.util.Arrays;
import java.util.List;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bootstrap.Bootstrap;

public class MockBootstrap extends Bootstrap
{

   private static final List<String> MOCK_WEB_BEAN_DISCOVERY_CLASS_NAMES = Arrays.asList(MockWebBeanDiscovery.class.getName()); 
   
   @Override
   protected List<String> getWebBeanDiscoveryClassNames()
   {
      return MOCK_WEB_BEAN_DISCOVERY_CLASS_NAMES;
   }
   
   public MockBootstrap(ManagerImpl manager)
   {
      super(manager);
   }
   
}
