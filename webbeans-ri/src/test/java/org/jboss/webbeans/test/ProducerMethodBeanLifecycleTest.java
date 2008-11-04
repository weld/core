package org.jboss.webbeans.test;

import org.testng.annotations.Test;

public class ProducerMethodBeanLifecycleTest
{

   @Test(groups="producerMethod") @SpecAssertion(section="3.3")
   public void testNonDependentProducerMethodThatReturnsNull()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.3")
   public void testDependentProducerMethodThatReturnsNull()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="specialization") @SpecAssertion(section="3.3.3")
   public void testSpecializedBeanAlwaysUsed()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"disposalMethod", "beanLifecycle"}) @SpecAssertion(section="3.3.4")
   public void testDisposalMethodCalled()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"disposalMethod", "beanLifecycle"}) @SpecAssertion(section="3.3.4")
   public void testDisposalMethodHasParametersInjected()
   {
      // TODO Placeholder
      assert false;
   }
   
}
