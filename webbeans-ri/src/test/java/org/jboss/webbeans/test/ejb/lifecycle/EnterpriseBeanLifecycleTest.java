package org.jboss.webbeans.test.ejb.lifecycle;

import javax.webbeans.UnremovedException;

import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.testng.annotations.Test;

public class EnterpriseBeanLifecycleTest extends AbstractTest
{

   @Test(groups={"removeMethod", "stub"}) @SpecAssertion(section="3.2.3")
   public void testInjectonOfParametersIntoRemoveMethod()
   {
      assert false;
   }
   
   @Test(groups={"specialization", "stub"}) @SpecAssertion(section="3.2.4")
   public void testSpecializedBeanAlwaysUsed()
   {
      assert false;
   }
   
   
   @Test(expectedExceptions = UnremovedException.class, groups={"enterpriseMethods", "removeMethod", "stub"})
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodNotExplicitlyDestroyedBeforeManagerAttemptFails()
   {
      assert false;
   }
   

   @Test(groups={"enterpriseBeans", "removeMethod", "stub"})
   @SpecAssertion(section = "3.3.5")
   public void testWebBeanRemoveMethodCallRemovesInstanceFromContext()
   {
      assert false;
   }
   

   @Test(groups={"enterpriseBeans", "removeMethod", "stub"})
   @SpecAssertion(section = "3.3.5")
   public void testNoParametersPassedWhenEnterpriseBeanRemoveMethodCalledFromApplication()
   {
      assert false;
   }
   
   @Test(groups={"enterpriseBeans", "removeMethod", "stub"})
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanRemoveMethodCalledOnDestroy()
   {
      assert false;
   }
   
   @Test(groups={"removeMethod", "enterpriseBeans", "stub"})
   @SpecAssertion(section = "3.3.5.3")
   public void testRemoveMethodParameters()
   {
      assert false;
   }
   
}
