package org.jboss.webbeans.test.tck;

import javax.webbeans.manager.Manager;

import org.jboss.webbeans.tck.api.Containers;
import org.jboss.webbeans.test.mock.MockBootstrap;
import org.jboss.webbeans.test.mock.MockWebBeanDiscovery;

public class ContainersImpl implements Containers
{
   
   public Manager deploy(Class<?>... classes)
   {
      MockBootstrap bootstrap = new MockBootstrap();
      bootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(classes));
      bootstrap.boot();
      return bootstrap.getManager();
   }
}
