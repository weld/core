package org.jboss.webbeans.test;

import java.util.Arrays;

import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.contexts.ApplicationContext;
import org.jboss.webbeans.contexts.RequestContext;
import org.jboss.webbeans.contexts.SessionContext;
import org.jboss.webbeans.contexts.SimpleBeanMap;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.mock.MockBootstrap;
import org.jboss.webbeans.test.mock.MockEjbDescriptor;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.testng.annotations.BeforeMethod;

public class AbstractTest
{
   
   protected MockManagerImpl manager;
   protected MockBootstrap webBeansBootstrap;
   
   @BeforeMethod
   public final void before()
   {
      manager = new MockManagerImpl();
      webBeansBootstrap = new MockBootstrap(manager);
      manager.addContext(RequestContext.INSTANCE);
      SessionContext.INSTANCE.setBeanMap(new SimpleBeanMap());
      manager.addContext(SessionContext.INSTANCE);
      ApplicationContext.INSTANCE.setBeanMap(new SimpleBeanMap());
      manager.addContext(ApplicationContext.INSTANCE);
      // Load the built in beans
      webBeansBootstrap.registerStandardBeans();
      init();
   }
   
   protected void init()
   {
      addStandardDeploymentTypesForTests();
   }
   
   protected void addStandardDeploymentTypesForTests()
   {
      manager.setEnabledDeploymentTypes(Arrays.asList(Standard.class, Production.class, AnotherDeploymentType.class, HornedAnimalDeploymentType.class));
   }

   protected <T> void addToEjbCache(Class<T> clazz)
   {
      manager.getEjbDescriptorCache().add(new MockEjbDescriptor<T>(clazz));
   }
   
}
