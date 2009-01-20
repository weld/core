package org.jboss.webbeans.test.unit.implementation;

import org.jboss.webbeans.test.unit.AbstractTest;
import org.testng.annotations.Test;

public class RemoveMethodTests extends AbstractTest
{

   /**
    * If the application directly calls an EJB remove method of an instance of a
    * session bean that is a stateful session bean and declares any scope other
    * than @Dependent, an UnsupportedOperationException is thrown.
    */
   @Test(groups = { "enterprisebean", "removemethod", "lifecycle", "stub" })
   public void testApplicationCalledRemoveMethodOfStatefulSessionBeanWithNonDependentScopeFails()
   {
      assert false;
   }

   /**
    * If the application directly calls an EJB remove method of an instance of a
    * session bean that is a stateful session bean and has scope @Dependent then
    * no parameters are passed to the method by the container.
    */
   @Test(groups = { "enterprisebean", "removemethod", "lifecycle", "stub" })
   public void testApplicationCalledRemoveMethodOfStatefulSessionBeanWithDependentScopeHasNoInjectedParameters()
   {
      deployBeans(Bad.class);
      BadLocal x = manager.getInstanceByType(BadLocal.class);
      x.bye();
   }

   /**
    * Furthermore, the container ignores the instance instead of destroying it
    * when Bean.destroy() is called, as defined in Section 6.5, “Lifecycle of
    * stateful session beans”.
    */
   @Test(groups = { "enterprisebean", "removemethod", "lifecycle", "stub" })
   public void testApplicationCalledRemoveMethodOfStatefulSessionBeanWithDependentScopeIsIgnoredWhenDestroyed()
   {
      assert false;
   }

}
