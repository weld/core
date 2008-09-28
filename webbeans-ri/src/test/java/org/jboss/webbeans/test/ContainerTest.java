package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bindings.StandardAnnotationLiteral;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.HornedAnimalDeploymentTypeAnnotationLiteral;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.testng.annotations.Test;

public class ContainerTest
{
   
   @Test
   public void testDefaultEnabledDeploymentTypes()
   {
      ManagerImpl container = new MockContainerImpl(null);
      assert container.getEnabledDeploymentTypes().size() == 2;
      assert container.getEnabledDeploymentTypes().get(0).annotationType().equals(Standard.class);
      assert container.getEnabledDeploymentTypes().get(1).annotationType().equals(Production.class);
   }

   @Test
   public void testCustomDeploymentTypes()
   {
      List<Annotation> enabledDeploymentTypes = new ArrayList<Annotation>();
      enabledDeploymentTypes.add(new StandardAnnotationLiteral());
      enabledDeploymentTypes.add(new AnotherDeploymentTypeAnnotationLiteral());
      enabledDeploymentTypes.add(new HornedAnimalDeploymentTypeAnnotationLiteral());
      ManagerImpl container = new MockContainerImpl(enabledDeploymentTypes);
      assert container.getEnabledDeploymentTypes().size() == 3;
      assert container.getEnabledDeploymentTypes().get(0).annotationType().equals(Standard.class);
      assert container.getEnabledDeploymentTypes().get(1).annotationType().equals(AnotherDeploymentType.class);
      assert container.getEnabledDeploymentTypes().get(2).annotationType().equals(HornedAnimalDeploymentType.class);
   }
   
   @Test
   public void testStandardMustBeDeclared()
   {
      List<Annotation> enabledDeploymentTypes = new ArrayList<Annotation>();
      enabledDeploymentTypes.add(new AnotherDeploymentTypeAnnotationLiteral());
      enabledDeploymentTypes.add(new HornedAnimalDeploymentTypeAnnotationLiteral());
      boolean exception = false;
      try
      {
         new MockContainerImpl(enabledDeploymentTypes);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
}
