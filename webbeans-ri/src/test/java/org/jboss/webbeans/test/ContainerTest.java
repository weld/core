package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.DeploymentException;
import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.testng.annotations.Test;

public class ContainerTest extends AbstractTest
{
   
   @Test
   public void testDefaultEnabledDeploymentTypes()
   {
      ManagerImpl container = new MockManagerImpl(null);
      assert container.getEnabledDeploymentTypes().size() == 2;
      assert container.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert container.getEnabledDeploymentTypes().get(1).equals(Production.class);
   }

   @Test
   public void testCustomDeploymentTypes()
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(Standard.class);
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      enabledDeploymentTypes.add(HornedAnimalDeploymentType.class);
      ManagerImpl container = new MockManagerImpl(enabledDeploymentTypes);
      assert container.getEnabledDeploymentTypes().size() == 3;
      assert container.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert container.getEnabledDeploymentTypes().get(1).equals(AnotherDeploymentType.class);
      assert container.getEnabledDeploymentTypes().get(2).equals(HornedAnimalDeploymentType.class);
   }
   
   @Test(expectedExceptions=DeploymentException.class)
   public void testStandardMustBeDeclared()
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      enabledDeploymentTypes.add(HornedAnimalDeploymentType.class);
      new MockManagerImpl(enabledDeploymentTypes);
   }
   
}
