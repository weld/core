package org.jboss.webbeans.test;

import javax.webbeans.UnproxyableDependencyException;

import org.testng.annotations.Test;

@SpecVersion("PDR")
public class ClientProxyTest
{
   

   @Test(groups="clientProxy") @SpecAssertion(section={"4.4", "4.8"})
   public void testClientProxyUsedForNormalScope()
   {
      assert false;
   }
   
   @Test(groups="clientProxy") @SpecAssertion(section={"4.4", "4.8"})
   public void testClientProxyNotUsedForPseudoScope()
   {
      assert false;
   }
   
   @Test(groups="clientProxy") @SpecAssertion(section="4.4")
   public void testClientProxyIsSerializable()
   {
      assert false;
   }
   
   @Test(groups="clientProxy", expectedExceptions=UnproxyableDependencyException.class) @SpecAssertion(section="4.4.1")
   public void testInjectionPointWithUnproxyableTypeResolvesToNormalScopedWebBean()
   {
      assert false;
   }
   
   @Test(groups="clientProxy") @SpecAssertion(section="4.4.2")
   public void testClientProxyInvocation()
   {
      assert false;
   }
   
   /*

   @Test(groups="clientProxy") @SpecAssertion(section="4.4")
   public void test
   {
      assert false;
   }

   */    

   
}
