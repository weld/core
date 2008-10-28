package org.jboss.webbeans.test;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.RequestScoped;
import javax.webbeans.manager.Context;

import org.jboss.webbeans.contexts.NormalContext;
import org.jboss.webbeans.contexts.RequestContext;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class ManagerTest extends AbstractTest
{
   
   @Override
   protected void addBuiltInContexts()
   {
      // No -op, done manually here
   }
   
   @Test(groups={"manager", "injection"}) @SpecAssertion(section="4.8")
   public void testInjectingManager()
   {
      assert false;
   }
   
   @Test(expectedExceptions={ContextNotActiveException.class}, groups={"manager"}) @SpecAssertion(section="8.6")
   public void testGetContextWithNoActiveContextsFails()
   {
      Context requestContext = new RequestContext();
      ((NormalContext)requestContext).setActive(false);
      manager.addContext(requestContext);
      manager.getContext(RequestScoped.class);
   }

   @Test(expectedExceptions={IllegalArgumentException.class}, groups={"manager"}) @SpecAssertion(section="8.6")
   public void testGetContextWithTooManyActiveContextsFails()
   {
      Context firstContext = new RequestContext();
      Context secondContext = new RequestContext();
      manager.addContext(firstContext);
      manager.addContext(secondContext);
      manager.getContext(RequestScoped.class);
      assert true;
   }

   @Test(expectedExceptions={ContextNotActiveException.class}, groups={"manager"}) @SpecAssertion(section="8.6")
   public void testGetContextWithNoRegisteredContextsFails()
   {
      manager.getContext(RequestScoped.class);
      assert false;
   }

   @Test(groups={"manager"}) @SpecAssertion(section="8.6")
   public void testGetContextReturnsActiveContext()
   {
      Context requestContext = new RequestContext();
      manager.addContext(requestContext);
      Context testContext = manager.getContext(RequestScoped.class);
      assert testContext == requestContext;
      
   }

   @Test(groups={"manager", "ejb3"}) @SpecAssertion(section="4.8")
   public void testManagerLookupInJndi()
   {
      assert false;
   }
   
   /*
   
   @Test(groups="manager") @SpecAssertion(section="4.8")
   public void test
   {
      assert false;
   }
   
   */
}
