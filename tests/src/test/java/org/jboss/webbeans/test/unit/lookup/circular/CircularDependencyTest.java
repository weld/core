package org.jboss.webbeans.test.unit.lookup.circular;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class CircularDependencyTest extends AbstractWebBeansTest
{
  
   @Test
   public void testCircularInjectionOnTwoSimpleDependentBeans() throws Exception
   {
      getCurrentManager().getInstanceByType(Foo.class).getName();
      assert Foo.success;
      assert Bar.success;
   }
   
   @Test
   public void testDependentProducerMethodDeclaredOnDependentBeanWhichInjectsProducedBean() throws Exception
   {
      getCurrentManager().getInstanceByType(DependentSelfConsumingDependentProducer.class).ping();
   }
   
   @Test(groups="broken", timeOut=1000)
   public void testDependentSelfConsumingProducer() throws Exception
   {
      getCurrentManager().getInstanceByType(Violation.class).ping();
   }

   @Test(groups="broken", timeOut=1000)
   public void testDependentCircularConstructors() throws Exception
   {
      getCurrentManager().getInstanceByType(Fish.class);
   }
   
   @Test(groups="broken", timeOut=1000)
   public void testSelfConsumingConstructorsOnDependentBean() throws Exception
   {      
      getCurrentManager().getInstanceByType(Farm.class);
   }
}
