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
      //deployBeans(Pig.class, Food.class);
      getCurrentManager().getInstanceByType(Pig.class).getName();
      assert Pig.success;
      assert Food.success;
   }
   
   @Test
   public void testCircularInjectionOnOneNormalAndOneDependentBean() throws Exception
   {
      //deployBeans(Car.class, Petrol.class);
      new RunInDependentContext()
      {

         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(Car.class).getName();
            assert Petrol.success;
            assert Car.success;
         }
         
      }.run();
      
   }
   
   @Test
   public void testCircularInjectionOnOneDependentAndOneNormalBean() throws Exception
   {
      //deployBeans(Car.class, Petrol.class);
      new RunInDependentContext()
      {

         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(Petrol.class).getName();
            assert Petrol.success;
            assert Car.success;
         }
         
      }.run();
      
   }
   
   
   @Test
   public void testCircularInjectionOnTwoSimpleDependentBeans() throws Exception
   {
      //deployBeans(Foo.class, Bar.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(Foo.class).getName();
            assert Foo.success;
            assert Bar.success;
         }
         
      }.run();
   }
   
   @Test
   public void testDependentProducerMethodDeclaredOnDependentBeanWhichInjectsProducedBean() throws Exception
   {
      //deployBeans(DependentSelfConsumingDependentProducer.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(DependentSelfConsumingDependentProducer.class).ping();
         }
         
      }.run();
   }
   
   @Test
   public void testNormalProducerMethodDeclaredOnNormalBeanWhichInjectsProducedBean() throws Exception
   {
      //deployBeans(NormalSelfConsumingNormalProducer.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(NormalSelfConsumingNormalProducer.class).ping();
         }
         
      }.run();
   }
   
   @Test
   public void testNormalProducerMethodDeclaredOnDependentBeanWhichInjectsProducedBean() throws Exception
   {
      //deployBeans(DependentSelfConsumingNormalProducer.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(DependentSelfConsumingNormalProducer.class).ping();
         }
         
      }.run();
   }
   
   @Test
   public void testDependentProducerMethodDeclaredOnNormalBeanWhichInjectsProducedBean() throws Exception
   {
      //deployBeans(NormalSelfConsumingDependentProducer.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(NormalSelfConsumingDependentProducer.class).ping();
         }
         
      }.run();
   }
   
   @Test
   public void testNormalSelfConsumingProducer() throws Exception
   {
      //deployBeans(NormalLoopingProducer.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(Violation.class).ping();
         }
         
      }.run();
   }
   
   @Test(groups="broken", timeOut=1000)
   public void testDependentSelfConsumingProducer() throws Exception
   {
      //deployBeans(DependentLoopingProducer.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(Violation.class).ping();
         }
         
      }.run();
   }

   @Test(groups="broken", timeOut=1000)
   public void testDependentCircularConstructors() throws Exception
   {
      //deployBeans(Water.class, Fish.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(Fish.class);
         }
         
      }.run();
   }
   
   @Test
   public void testNormalCircularConstructors() throws Exception
   {
      //deployBeans(Bird.class, Air.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(Bird.class);
         }
         
      }.run();
   }
   
   @Test
   public void testNormalAndDependentCircularConstructors() throws Exception
   {
      //deployBeans(Space.class, Planet.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(Planet.class);
         }
         
      }.run();
   }
   
   @Test(groups="broken", timeOut=1000)
   public void testSelfConsumingConstructorsOnDependentBean() throws Exception
   {
      //deployBeans(Farm.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(Farm.class);
         }
         
      }.run();
   }
   
   @Test
   public void testSelfConsumingConstructorsOnNormalBean() throws Exception
   {
      //deployBeans(House.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            getCurrentManager().getInstanceByType(House.class);
         }
         
      }.run();
   }
   
}
