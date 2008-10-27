package org.jboss.webbeans.test;

import org.testng.annotations.Test;

@SpecVersion("PDR")
public class ManagerTest
{
   
   @Test(groups={"manager", "injection"}) @SpecAssertion(section="4.8")
   public void testInjectingManager()
   {
      assert false;
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
