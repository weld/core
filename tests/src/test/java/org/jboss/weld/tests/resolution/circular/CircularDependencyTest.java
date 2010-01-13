package org.jboss.weld.tests.resolution.circular;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class CircularDependencyTest extends AbstractWeldTest
{
  
   @Test
   public void testCircularInjectionOnTwoSimpleDependentBeans() throws Exception
   {
      getReference(Foo.class).getName();
      assert Foo.success;
      assert Bar.success;
   }
   
   @Test
   public void testDependentProducerMethodDeclaredOnDependentBeanWhichInjectsProducedBean() throws Exception
   {
      getReference(DependentSelfConsumingDependentProducer.class).ping();
   }
   
   @Test
   public void testDependentSelfConsumingProducer() throws Exception
   {
      getReference(Violation.class).ping();
   }

}
