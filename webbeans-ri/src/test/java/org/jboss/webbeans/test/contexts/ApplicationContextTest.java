package org.jboss.webbeans.test.contexts;

import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.testng.annotations.Test;

@SpecVersion("20081206")
public class ApplicationContextTest extends AbstractTest
{

   /**
    * The application scope is active during the service() method of any servlet
    * in the web application.
    */
   @Test(groups = { "stub", "contexts", "servlet" })
   @SpecAssertion(section = "9.6.3")
   public void testApplicationScopeActiveDuringServiceMethod()
   {
      assert false;
   }

   /**
    * The application scope is active during any Java EE web service invocation.
    */
   @Test(groups = { "stub", "contexts", "webservice" })
   @SpecAssertion(section = "9.6.3")
   public void testApplicationScopeActiveDuringWebSericeInvocation()
   {
      assert false;
   }

   /**
    * The application scope is also active during any remote method invocation
    * of any EJB bean, during any call to an EJB timeout method and during
    * message delivery to any EJB message driven bean.
    */
   @Test(groups = { "stub", "contexts", "ejb3" })
   @SpecAssertion(section = "9.6.3")
   public void testApplicationScopeActiveDuringRemoteMethodInvocationOfEjb()
   {
      assert false;
   }

   /**
    * The application scope is also active during any remote method invocation
    * of any EJB bean, during any call to an EJB timeout method and during
    * message delivery to any EJB message driven bean.
    */
   @Test(groups = { "stub", "contexts", "ejb3" })
   @SpecAssertion(section = "9.6.3")
   public void testApplicationScopeActiveDuringCallToEjbTimeoutMethod()
   {
      assert false;
   }

   /**
    * The application scope is also active during any remote method invocation
    * of any EJB bean, during any call to an EJB timeout method and during
    * message delivery to any EJB message driven bean.
    */
   @Test(groups = { "stub", "contexts", "ejb3" })
   @SpecAssertion(section = "9.6.3")
   public void testApplicationScopeActiveDuringEjbMessageDelivery()
   {
      assert false;
   }

   /**
    * The application context is shared between all servlet requests, web
    * service invocations, EJB remote method invocations, EJB timeouts and
    * message deliveries to message driven beans that execute within the same
    * application
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.3")
   public void testApplicationContextSharedBetweenInvokationsInApplication()
   {
      assert false;
   }

   /**
    * The application context is destroyed when the application is undeployed.
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.3")
   public void testApplicationScopeDestroyedWhenApplicationIsUndeployed()
   {
      assert false;
   }

}
