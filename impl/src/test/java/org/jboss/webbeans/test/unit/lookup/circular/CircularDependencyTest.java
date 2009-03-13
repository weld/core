package org.jboss.webbeans.test.unit.lookup.circular;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class CircularDependencyTest extends AbstractWebBeansTest
{
  
   
   @Test
   public void testCircularInjectionOnTwoNormalBeans() throws Exception
   {
      //deployBeans(Pig.class, Food.class);
      manager.getInstanceByType(Pig.class).getName();
      assert Pig.success;
      assert Food.success;
   }
   
   @Test(timeOut=1000)
   public void testCircularInjectionOnOneNormalAndOneDependentBean() throws Exception
   {
      //deployBeans(Car.class, Petrol.class);
      new RunInDependentContext()
      {

         @Override
         protected void execute() throws Exception
         {
            manager.getInstanceByType(Car.class).getName();
            assert Petrol.success;
            assert Car.success;
         }
         
      }.run();
      
   }
   
   @Test(timeOut=1000)
   public void testCircularInjectionOnOneDependentAndOneNormalBean() throws Exception
   {
      //deployBeans(Car.class, Petrol.class);
      new RunInDependentContext()
      {

         @Override
         protected void execute() throws Exception
         {
            manager.getInstanceByType(Petrol.class).getName();
            assert Petrol.success;
            assert Car.success;
         }
         
      }.run();
      
   }
   
   
   @Test(timeOut=1000)
   public void testCircularInjectionOnTwoSimpleDependentBeans() throws Exception
   {
      //deployBeans(Foo.class, Bar.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            manager.getInstanceByType(Foo.class).getName();
            assert Foo.success;
            assert Bar.success;
         }
         
      }.run();
   }
   
   @Test(timeOut=1000)
   public void testDependentProducerMethodDeclaredOnDependentBeanWhichInjectsProducedBean() throws Exception
   {
      //deployBeans(DependentSelfConsumingDependentProducer.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            manager.getInstanceByType(DependentSelfConsumingDependentProducer.class).ping();
         }
         
      }.run();
   }
   
   @Test(timeOut=1000)
   public void testNormalProducerMethodDeclaredOnNormalBeanWhichInjectsProducedBean() throws Exception
   {
      //deployBeans(NormalSelfConsumingNormalProducer.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            manager.getInstanceByType(NormalSelfConsumingNormalProducer.class).ping();
         }
         
      }.run();
   }
   
   @Test(timeOut=1000)
   public void testNormalProducerMethodDeclaredOnDependentBeanWhichInjectsProducedBean() throws Exception
   {
      //deployBeans(DependentSelfConsumingNormalProducer.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            manager.getInstanceByType(DependentSelfConsumingNormalProducer.class).ping();
         }
         
      }.run();
   }
   
   @Test(timeOut=1000)
   public void testDependentProducerMethodDeclaredOnNormalBeanWhichInjectsProducedBean() throws Exception
   {
      //deployBeans(NormalSelfConsumingDependentProducer.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            manager.getInstanceByType(NormalSelfConsumingDependentProducer.class).ping();
         }
         
      }.run();
   }
   
   @Test(timeOut=1000)
   public void testNormalSelfConsumingProducer() throws Exception
   {
      //deployBeans(NormalLoopingProducer.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            manager.getInstanceByType(Violation.class).ping();
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
            manager.getInstanceByType(Violation.class).ping();
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
            manager.getInstanceByType(Fish.class);
         }
         
      }.run();
   }
   
   @Test(timeOut=1000)
   public void testNormalCircularConstructors() throws Exception
   {
      //deployBeans(Bird.class, Air.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            manager.getInstanceByType(Bird.class);
         }
         
      }.run();
   }
   
   @Test(timeOut=1000)
   public void testNormalAndDependentCircularConstructors() throws Exception
   {
      //deployBeans(Space.class, Planet.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            manager.getInstanceByType(Planet.class);
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
            manager.getInstanceByType(Farm.class);
         }
         
      }.run();
   }
   
   @Test(timeOut=1000)
   public void testSelfConsumingConstructorsOnNormalBean() throws Exception
   {
      //deployBeans(House.class);
      new RunInDependentContext()
      {
       
         @Override
         protected void execute() throws Exception
         {
            manager.getInstanceByType(House.class);
         }
         
      }.run();
   }
   
}
