package org.jboss.webbeans.test.contexts;

import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.testng.annotations.Test;

@SpecVersion("2001206")
public class SessionContextTest extends AbstractTest
{

   /**
    * The session scope is active during the service() method of any servlet in
    * the web application
    */
   @Test(groups = { "stub", "contexts", "servlet" })
   @SpecAssertion(section = "9.6.2")
   public void testSessionScopeActiveDuringServiceMethod()
   {
      assert false;
   }

   /**
    * The session context is shared between all servlet requests that occur in
    * the same HTTP servlet session
    */
   @Test(groups = { "stub", "contexts", "servlet" })
   @SpecAssertion(section = "9.6.2")
   public void testSessionContextSharedBetweenServletRequestsInSameHttpSession()
   {
      assert false;
   }

   /**
    * The session context is destroyed when the HTTPSession is invalidated or
    * times out.
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.2")
   public void testSessionContextDestroyedWhenHttpSessionInvalidatedOrTimesOut()
   {
      assert false;
   }

}
