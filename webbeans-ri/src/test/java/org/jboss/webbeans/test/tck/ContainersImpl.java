package org.jboss.webbeans.test.tck;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.webbeans.manager.Manager;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.tck.api.Containers;
import org.jboss.webbeans.test.mock.MockBootstrap;
import org.jboss.webbeans.test.mock.MockWebBeanDiscovery;

public class ContainersImpl implements Containers
{
   
   public Manager deploy(List<Class<? extends Annotation>> enabledDeploymentTypes, Class<?>... classes)
   {
      MockBootstrap bootstrap = new MockBootstrap();
      bootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(classes));
      bootstrap.boot();
      ManagerImpl manager = bootstrap.getManager();
      if (enabledDeploymentTypes != null)
      {
         manager.setEnabledDeploymentTypes(enabledDeploymentTypes);
      }
      return manager;
   }
   
   public Manager deploy(java.lang.Class<?>... classes) 
   {
      return deploy(null, classes);
   }
}
