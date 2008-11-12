package org.jboss.webbeans.test;

import org.testng.annotations.Test;

@SpecVersion("PDR")
public class RequestContextTest extends AbstractTest
{
   
   @Test(groups={"stub", "servlet", "contexts"}) @SpecAssertion(section="8.5.1")
   public void testScopeActiveDuringServiceMethod()
   {
      assert false;
   }
   
   @Test(groups={"stub", "webservice", "contexts"}) @SpecAssertion(section="8.5.1")
   public void testScopeActiveDuringWebServiceInvocation()
   {
      assert false;
   }
   
   @Test(groups={"stub", "contexts", "ejb3"}) @SpecAssertion(section="8.5.1")
   public void testScopeActiveDuringRemoteInvocationOfEjbs()
   {
      assert false;
   }
   
   @Test(groups={"stub", "contexts", "ejb3"}) @SpecAssertion(section="8.5.1")
   public void testScopeActiveDuringEjbTimeoutsInEE6()
   {
      assert false;
   }
   
   @Test(groups={"stub", "contexts", "ejb3"}) @SpecAssertion(section="8.5.1")
   public void testScopeActiveDuringMessageDelivery()
   {
      assert false;
   }
   
   @Test(groups={"stub", "servlet", "contexts"}) @SpecAssertion(section="8.5.1")
   public void testScopeNotSharedAcrossRequests()
   {
      assert false;
   }
   
   @Test(groups={"stub", "webservice", "contexts"}) @SpecAssertion(section="8.5.1")
   public void testScopeNotSharedAcrossWebServiceInvocations()
   {
      assert false;
   }
   
   @Test(groups={"stub", "contexts", "ejb3"}) @SpecAssertion(section="8.5.1")
   public void testScopeNotSharedAcrossRemoteInvocationsOfEjbs()
   {
      assert false;
   }
   
   @Test(groups={"stub", "contexts", "ejb3"}) @SpecAssertion(section="8.5.1")
   public void testScopeNotSharedAcrossEjbTimeouts()
   {
      assert false;
   }
   
   @Test(groups={"stub", "contexts", "ejb3"}) @SpecAssertion(section="8.5.1")
   public void testScopeNotSharedAcrossMessageDelivery()
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
