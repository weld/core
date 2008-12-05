package org.jboss.webbeans.test;

import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bootstrap.Bootstrap;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.mock.MockBootstrap;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.testng.annotations.BeforeMethod;

public class AbstractTest
{
   
   protected MockManagerImpl manager;
   protected Bootstrap bootstrap;

   
   @BeforeMethod
   public final void before()
   {
      manager = new MockManagerImpl();
      ManagerImpl.setInstance(manager);
      bootstrap = new MockBootstrap();
      init();
   }
   
   protected void init()
   {
      addEnabledDeploymentTypes();
   }
   
   protected void addEnabledDeploymentTypes()
   {
      manager.setEnabledDeploymentTypes(Standard.class, Production.class, AnotherDeploymentType.class, HornedAnimalDeploymentType.class);
   }

}
