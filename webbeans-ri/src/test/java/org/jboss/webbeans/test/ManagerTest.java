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
   
   @Test(groups={"manager", "injection", "deployment"}) @SpecAssertion(section="4.8")
   public void testInjectingManager()
   {
      assert false;
   }
   
   @Test(expectedExceptions={ContextNotActiveException.class}, groups={"manager"}) @SpecAssertion(section="8.6")
   public void testGetContextWithNoActiveContextsFails()
   {
      Context requestContext = new RequestContext();
      ((NormalContext)requestContext).setActive(false);
      manager.setContexts(requestContext);
      manager.getContext(RequestScoped.class);
   }

   @Test(expectedExceptions={IllegalArgumentException.class}, groups={"manager"}) @SpecAssertion(section="8.6")
   public void testGetContextWithTooManyActiveContextsFails()
   {
      Context firstContext = new RequestContext();
      Context secondContext = new RequestContext();
      manager.setContexts(firstContext, secondContext);
      manager.getContext(RequestScoped.class);
      assert true;
   }

   @Test(expectedExceptions={ContextNotActiveException.class}, groups={"manager"}) @SpecAssertion(section="8.6")
   public void testGetContextWithNoRegisteredContextsFails()
   {
      manager.setContexts();
      manager.getContext(RequestScoped.class);
      assert false;
   }

   @Test(groups={"manager"}) @SpecAssertion(section="8.6")
   public void testGetContextReturnsActiveContext()
   {
      Context requestContext = new RequestContext();
      manager.setContexts(requestContext);
      Context testContext = manager.getContext(RequestScoped.class);
      assert testContext == requestContext;
      
   }

   @Test(groups={"manager", "ejb3"}) @SpecAssertion(section="4.8")
   public void testManagerLookupInJndi()
   {
      assert false;
   }

   @Test(groups="manager")
   public void testWrappingOfBeanCollection() 
   {
      // TODO stub
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
