package org.jboss.webbeans.test;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.NullableDependencyException;

import org.testng.annotations.Test;

@SpecVersion("PDR")
public class InjectionTests extends AbstractTest
{
   
   @Test(groups="injection") @SpecAssertion(section="4.2")
   public void testPrimitiveTypesEquivalentToBoxedTypes()
   {
      assert false;
   }
   
   @Test(groups="injection") @SpecAssertion(section="4.2")
   public void testInjectionPerformsBoxingIfNecessary()
   {
      assert false;
   }
   
   @Test(groups="injection") @SpecAssertion(section="4.2")
   public void testInjectionPerformsUnboxingIfNecessary()
   {
      assert false;
   }
   
   @Test(groups="injection", expectedExceptions=NullableDependencyException.class) @SpecAssertion(section="4.2")
   public void testPrimitiveInjectionPointResolvesToNullableWebBean()
   {
      assert false;
   }
   
   @Test(groups="injection", expectedExceptions=ContextNotActiveException.class) @SpecAssertion(section="4.3")
   public void testInvokeNormalInjectedWebBeanWhenContextNotActive()
   {
      assert false;
   }
   
   @Test(groups="injection") @SpecAssertion(section="4.3")
   public void testInovkeDependentScopeWhenContextNotActive()
   {
      assert false;
   }
   
  /*

   @Test(groups="injection") @SpecAssertion(section="4.2")
   public void test
   {
      assert false;
   }

   */    
}
