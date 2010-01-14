package org.jboss.weld.tests.ejb;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
@IntegrationTest
public class EJBTest extends AbstractWeldTest
{
   
   @Test(groups="broken")
   public void testNoInterface()
   {
      Cow cow = getReference(Cow.class);
      cow.ping();
      assert cow.isPinged();
   }
   
}
