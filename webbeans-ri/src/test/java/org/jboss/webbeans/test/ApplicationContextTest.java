package org.jboss.webbeans.test;

import org.testng.annotations.Test;

@SpecVersion("2001206")
public class ApplicationContextTest extends AbstractTest
{
   
   @Test(groups={"stub", "servlet", "contexts"}) @SpecAssertion(section="9.5.3")
   public void testScopeActiveDuringServiceMethod()
   {
      assert false;
   }
   
   @Test(groups={"stub", "webservice", "contexts"}) @SpecAssertion(section="9.5.3")
   public void testScopeActiveDuringWebServiceInvocation()
   {
      assert false;
   }
   
   @Test(groups={"stub", "contexts", "ejb3"}) @SpecAssertion(section="9.5.3")
   public void testScopeActiveDuringRemoteInvocationOfEjbs()
   {
      assert false;
   }
   
   @Test(groups={"stub", "contexts", "ejb3"}) @SpecAssertion(section="9.5.3")
   public void testScopeActiveDuringEjbTimeouts()
   {
      assert false;
   }
   
   @Test(groups={"stub", "contexts", "ejb3"}) @SpecAssertion(section="9.5.3")
   public void testScopeActiveDuringMessageDelivery()
   {
      assert false;
   }
   
   @Test(groups={"stub", "servlet", "contexts"}) @SpecAssertion(section="9.5.3")
   public void testScopeSharedAcrossRequests()
   {
      assert false;
   }
   
   @Test(groups={"stub", "webservice", "contexts"}) @SpecAssertion(section="9.5.3")
   public void testScopeSharedAcrossWebServiceInvocations()
   {
      assert false;
   }
   
   @Test(groups={"stub", "contexts", "ejb3"}) @SpecAssertion(section="9.5.3")
   public void testScopeSharedAcrossRemoteInvocationsOfEjbs()
   {
      assert false;
   }
   
   @Test(groups={"stub", "contexts", "ejb3"}) @SpecAssertion(section="9.5.3")
   public void testScopeSharedAcrossEjbTimeouts()
   {
      assert false;
   }
   
   @Test(groups={"stub", "contexts", "ejb3"}) @SpecAssertion(section="9.5.3")
   public void testScopeSharedAcrossMessageDelivery()
   {
      assert false;
   }
   
   /*

   @Test @SpecAssertion(section="9.5.3")
   public void test
   {
      assert false;
   }

    */
   
}
