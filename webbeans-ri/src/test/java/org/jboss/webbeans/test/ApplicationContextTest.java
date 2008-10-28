package org.jboss.webbeans.test;

import org.testng.annotations.Test;

public class ApplicationContextTest extends AbstractTest
{
   
   @Test(groups={"servlet", "contexts"}) @SpecAssertion(section="8.5.3")
   public void testScopeActiveDuringServiceMethod()
   {
      assert false;
   }
   
   @Test(groups={"webservice", "contexts"}) @SpecAssertion(section="8.5.3")
   public void testScopeActiveDuringWebServiceInvocation()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.5.3")
   public void testScopeActiveDuringRemoteInvocationOfEjbs()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.5.3")
   public void testScopeActiveDuringEjbTimeoust()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.5.3")
   public void testScopeActiveDuringMessageDelivery()
   {
      assert false;
   }
   
   @Test(groups={"servlet", "contexts"}) @SpecAssertion(section="8.5.3")
   public void testScopeSharedAcrossRequests()
   {
      assert false;
   }
   
   @Test(groups={"webservice", "contexts"}) @SpecAssertion(section="8.5.3")
   public void testScopeSharedAcrossWebServiceInvocations()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.5.3")
   public void testScopeSharedAcrossRemoteInvocationsOfEjbs()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.5.3")
   public void testScopeSharedAcrossEjbTimeouts()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.5.3")
   public void testScopeSharedAcrossMessageDelivery()
   {
      assert false;
   }
   
   /*

   @Test @SpecAssertion(section="8.5.3")
   public void test
   {
      assert false;
   }

    */
   
}
