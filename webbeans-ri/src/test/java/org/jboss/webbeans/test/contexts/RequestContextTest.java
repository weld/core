package org.jboss.webbeans.test.contexts;

import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.testng.annotations.Test;

@SpecVersion("20081206")
public class RequestContextTest extends AbstractTest
{

   /**
    * The request scope is active during the service() method of any Servlet in
    * the web application.
    */
   @Test(groups = { "stub", "contexts", "servlet" })
   @SpecAssertion(section = "9.6.1")
   public void testRequestScopeActiveDuringServiceMethod()
   {
      assert false;
   }

   /**
    * The request context is destroyed at the end of the servlet request, after
    * the Servlet service() method returns.
    */
   @Test(groups = { "stub", "contexts", "servlet" })
   @SpecAssertion(section = "9.6.1")
   public void testRequestScopeIsDestroyedAfterServiceMethod()
   {
      assert false;
   }

   /**
    * The request scope is active during any Java EE web service invocation.
    */
   @Test(groups = { "stub", "contexts", "webservice" })
   @SpecAssertion(section = "9.6.1")
   public void testRequestScopeActiveDuringWebSericeInvocation()
   {
      assert false;
   }

   /**
    * The request context is destroyed after the web service invocation
    * completes
    */
   @Test(groups = { "stub", "contexts", "webservice" })
   @SpecAssertion(section = "9.6.1")
   public void testRequestScopeIsDestroyedAfterWebServiceInvocation()
   {
      assert false;
   }

   /**
    * The request scope is active during any remote method invocation of any EJB
    * bean, during any call to an EJB timeout method and during message delivery
    * to any EJB message driven bean.
    */
   @Test(groups = { "stub", "contexts", "ejb3" })
   @SpecAssertion(section = "9.6.1")
   public void testRequestScopeActiveDuringRemoteMethodInvocationOfEjb()
   {
      assert false;
   }

   /**
    * The request scope is active during any remote method invocation of any EJB
    * bean, during any call to an EJB timeout method and during message delivery
    * to any EJB message driven bean.
    */
   @Test(groups = { "stub", "contexts", "ejb3" })
   @SpecAssertion(section = "9.6.1")
   public void testRequestScopeActiveDuringCallToEjbTimeoutMethod()
   {
      assert false;
   }

   /**
    * The request scope is active during any remote method invocation of any EJB
    * bean, during any call to an EJB timeout method and during message delivery
    * to any EJB message driven bean.
    */
   @Test(groups = { "stub", "contexts", "ejb3" })
   @SpecAssertion(section = "9.6.1")
   public void testRequestScopeActiveDuringEjbMessageDelivery()
   {
      assert false;
   }

   /**
    * The request context is destroyed after the remote method invocation,
    * timeout or message delivery completes.
    */
   @Test(groups = { "stub", "contexts", "ejb3" })
   @SpecAssertion(section = "9.6.1")
   public void testRequestScopeDestroyedAfterRemoteMethodInvocationOfEjb()
   {
      assert false;
   }

   /**
    * The request context is destroyed after the remote method invocation,
    * timeout or message delivery completes.
    */
   @Test(groups = { "stub", "contexts", "ejb3" })
   @SpecAssertion(section = "9.6.1")
   public void testRequestScopeDestroyedAfterCallToEjbTimeoutMethod()
   {
      assert false;
   }

   /**
    * The request context is destroyed after the remote method invocation,
    * timeout or message delivery completes.
    */
   @Test(groups = { "stub", "contexts", "ejb3" })
   @SpecAssertion(section = "9.6.1")
   public void testRequestScopeDestroyedAfterEjbMessageDelivery()
   {
      assert false;
   }

}
