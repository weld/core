package org.jboss.webbeans.test;

import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.testng.annotations.Test;


public class DefaultDeploymentTypeTest extends AbstractTest
{
   
   @Override
   protected void addStandardDeploymentTypesForTests()
   {
      // No-op
   }
   
   @Test @SpecAssertion(section={"2.5.6", "2.5.7"})
   public void testDefaultEnabledDeploymentTypes()
   {
      assert manager.getEnabledDeploymentTypes().size() == 2;
      assert manager.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert manager.getEnabledDeploymentTypes().get(1).equals(Production.class);
   }
   
}
