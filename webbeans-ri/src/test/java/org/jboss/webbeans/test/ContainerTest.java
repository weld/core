package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.bindings.StandardBinding;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeBinding;
import org.jboss.webbeans.test.bindings.HornedAnimalDeploymentTypeBinding;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.junit.Test;

public class ContainerTest
{
   
   @Test
   public void testDefaultEnabledDeploymentTypes()
   {
      ContainerImpl container = new MockContainerImpl(null);
      assert container.getEnabledDeploymentTypes().size() == 2;
      assert container.getEnabledDeploymentTypes().get(0).annotationType().equals(Standard.class);
      assert container.getEnabledDeploymentTypes().get(1).annotationType().equals(Production.class);
   }

   @Test
   public void testCustomDeploymentTypes()
   {
      List<Annotation> enabledDeploymentTypes = new ArrayList<Annotation>();
      enabledDeploymentTypes.add(new StandardBinding());
      enabledDeploymentTypes.add(new AnotherDeploymentTypeBinding());
      enabledDeploymentTypes.add(new HornedAnimalDeploymentTypeBinding());
      ContainerImpl container = new MockContainerImpl(enabledDeploymentTypes);
      assert container.getEnabledDeploymentTypes().size() == 3;
      assert container.getEnabledDeploymentTypes().get(0).annotationType().equals(Standard.class);
      assert container.getEnabledDeploymentTypes().get(1).annotationType().equals(AnotherDeploymentType.class);
      assert container.getEnabledDeploymentTypes().get(2).annotationType().equals(HornedAnimalDeploymentType.class);
   }
   
   @Test
   public void testStandardMustBeDeclared()
   {
      List<Annotation> enabledDeploymentTypes = new ArrayList<Annotation>();
      enabledDeploymentTypes.add(new AnotherDeploymentTypeBinding());
      enabledDeploymentTypes.add(new HornedAnimalDeploymentTypeBinding());
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
