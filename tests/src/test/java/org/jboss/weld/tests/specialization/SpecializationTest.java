package org.jboss.weld.tests.specialization;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class SpecializationTest extends AbstractWeldTest
{
   
   @Test(groups="WELD-321")
   public void testSpecialization()
   {
      assert getCurrentManager().resolve(getCurrentManager().getBeans(User.class)).getBeanClass().equals(User2.class);
   }

}
