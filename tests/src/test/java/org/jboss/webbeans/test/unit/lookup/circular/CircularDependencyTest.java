package org.jboss.webbeans.test.unit.lookup.circular;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class CircularDependencyTest extends AbstractWebBeansTest
{
  
   
   @Test
   public void testCircularInjectionOnTwoNormalBeans() throws Exception
   {
      getCurrentManager().getInstanceByType(Pig.class).getName();
      assert Pig.success;
      assert Food.success;
   }
   
   @Test
   public void testCircularInjectionOnOneNormalAndOneDependentBean() throws Exception
   {
      
      getCurrentManager().getInstanceByType(Car.class).getName();
      assert Petrol.success;
      assert Car.success;
   }
   
   @Test
   public void testCircularInjectionOnOneDependentAndOneNormalBean() throws Exception
   {
      getCurrentManager().getInstanceByType(Petrol.class).getName();
      assert Petrol.success;
      assert Car.success;
   }
   
   
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
   
   @Test
   public void testNormalProducerMethodDeclaredOnNormalBeanWhichInjectsProducedBean() throws Exception
   {
      getCurrentManager().getInstanceByType(NormalSelfConsumingNormalProducer.class).ping();
   }
   
   @Test
   public void testNormalProducerMethodDeclaredOnDependentBeanWhichInjectsProducedBean() throws Exception
   {
      getCurrentManager().getInstanceByType(DependentSelfConsumingNormalProducer.class).ping();
   }
   
   @Test
   public void testDependentProducerMethodDeclaredOnNormalBeanWhichInjectsProducedBean() throws Exception
   {
      getCurrentManager().getInstanceByType(NormalSelfConsumingDependentProducer.class).ping();
   }
   
   @Test
   public void testNormalSelfConsumingProducer() throws Exception
   {
      createContextualInstance(Violation.class).ping();
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
   
   @Test
   public void testNormalCircularConstructors() throws Exception
   {
      getCurrentManager().getInstanceByType(Bird.class);
   }
   
   @Test
   public void testNormalAndDependentCircularConstructors() throws Exception
   {      
      getCurrentManager().getInstanceByType(Planet.class);
   }
   
   @Test(groups="broken", timeOut=1000)
   public void testSelfConsumingConstructorsOnDependentBean() throws Exception
   {      
      getCurrentManager().getInstanceByType(Farm.class);
   }
   
   @Test
   public void testSelfConsumingConstructorsOnNormalBean() throws Exception
   {      
      getCurrentManager().getInstanceByType(House.class);
   }
   
}
