package org.jboss.webbeans.test;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.RequestScoped;
import javax.webbeans.manager.Context;

import org.jboss.webbeans.context.RequestContext;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class ManagerTest extends AbstractTest
{
  
   
   @Test(expectedExceptions={ContextNotActiveException.class}, groups={"manager"}) @SpecAssertion(section="8.6")
   public void testGetContextWithNoActiveContextsFails()
   {
      RequestContext.INSTANCE.setActive(false);
      manager.getContext(RequestScoped.class);
   }

   @Test(expectedExceptions={IllegalArgumentException.class}, groups={"manager"}) @SpecAssertion(section="8.6")
   public void testGetContextWithTooManyActiveContextsFails()
   {
      Context firstContext = new RequestContext() {};
      Context secondContext = new RequestContext() {};
      manager.addContext(firstContext);
      manager.addContext(secondContext);
      manager.getContext(RequestScoped.class);
      assert true;
   }

   @Test(expectedExceptions={ContextNotActiveException.class}, groups={"stub", "manager"}) @SpecAssertion(section="8.6")
   public void testGetContextWithNoRegisteredContextsFails()
   {
      manager.getContext(RequestScoped.class);
      assert false;
   }

   @Test(groups={"manager"}) @SpecAssertion(section="8.6")
   public void testGetContextReturnsActiveContext()
   {
      manager.getContext(RequestScoped.class);
   }

   /*
   
   @Test(groups="manager") @SpecAssertion(section="5.8")
   public void test
   {
      assert false;
   }
   
   */
}
