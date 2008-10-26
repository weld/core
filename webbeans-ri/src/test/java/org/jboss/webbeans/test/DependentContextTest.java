package org.jboss.webbeans.test;

import org.testng.annotations.Test;

public class DependentContextTest extends AbstractTest
{
   
   @Test(groups="contexts") @SpecAssertion(section="8.3")
   public void testInstanceNotSharedBetweenInjectionPoints()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "el"}) @SpecAssertion(section="8.3")
   public void testInstanceUsedForElEvalutionNotShared()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "producerMethod"}) @SpecAssertion(section="8.3")
   public void testInstanceUsedForProducerMethodNotShared()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "eventbus"}) @SpecAssertion(section="8.3")
   public void testInstanceUsedForObserverMethodNotShared()
   {
      assert false;
   }
   
   @Test(groups="contexts") @SpecAssertion(section="8.3")
   public void testContextGetWithCreateTrueReturnsNewInstance()
   {
      assert false;
   }
   
   @Test(groups="contexts") @SpecAssertion(section="8.3")
   public void testContextGetWithCreateFalseReturnsNull()
   {
      assert false;
   }
   
   @Test(groups="contexts") @SpecAssertion(section="8.3")
   public void testContextIsInactive()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "producerMethod"}) @SpecAssertion(section="8.3")
   public void testContextIsActiveWhenInvokingProducerMethod()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "eventbus"}) @SpecAssertion(section="8.3")
   public void testContextIsActiveWhenInvokingObserverMethod()
   {
      assert false;
   }
   
   
   @Test(groups={"contexts", "el"}) @SpecAssertion(section="8.3")
   public void testContextIsActiveWhenEvaluatingElExpression()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "beanLifecycle"}) @SpecAssertion(section="8.3")
   public void testContextIsActiveDuringBeanCreation()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "beanLifecycle"}) @SpecAssertion(section="8.3")
   public void testContextIsActiveDuringBeanDestruction()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "injection"}) @SpecAssertion(section="8.3")
   public void testContextIsActiveDuringInjection()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.3")
   public void testEjbBeanMayMayCreateInstanceFromInitializer()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.3")
   public void testEjbBeanMayMayCreateInstanceFromPostConstruct()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.3")
   public void testEjbBeanMayMayCreateInstanceFromPreDestroy()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "servlet"}) @SpecAssertion(section="8.3")
   public void testServletBeanMayMayCreateInstanceFromInitializer()
   {
      assert false;
   }
   
   @Test(groups={"contexts"}) @SpecAssertion(section="8.3")
   public void testDestroyingParentDestroysDependents()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.3")
   public void testDestroyingEjbDestroysDependents()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "servlet"}) @SpecAssertion(section="8.3")
   public void testDestroyingServletDestroysDependents()
   {
      assert false;
   }
   
   @Test(groups={"contexts"}) @SpecAssertion(section="8.3")
   public void testDependentsDestroyedWhenElEvaluationCompletes()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "producerMethod"}) @SpecAssertion(section="8.3")
   public void testDependentsDestroyedWhenProducerMethodEvaluationCompletes()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "eventbus"}) @SpecAssertion(section="8.3")
   public void testDependentsDestroyedWhenObserverMethodEvaluationCompletes()
   {
      assert false;
   }
   
}
