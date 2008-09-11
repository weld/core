package org.jboss.webbeans.test;

import org.testng.annotations.Test;

public class ProducerMethodComponentLifecycleTest
{

   @Test(groups="componentLifecycle") @SpecAssertion(section="3.3")
   public void testNonDependentProducerMethodThatReturnsNull()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="componentLifecycle") @SpecAssertion(section="3.3")
   public void testDependentProducerMethodThatReturnsNull()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="specialization") @SpecAssertion(section="3.3.3")
   public void testSpecializedComponentAlwaysUsed()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"disposalMethod", "componentLifecycle"}) @SpecAssertion(section="3.3.4")
   public void testDisposalMethodCalled()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"disposalMethod", "componentLifecycle"}) @SpecAssertion(section="3.3.4")
   public void testDisposalMethodHasParametersInjected()
   {
      // TODO Placeholder
      assert false;
   }
   
}
