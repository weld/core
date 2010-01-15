package org.jboss.weld.tests.extensions.annotatedType;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.jsr299.Extension;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@Packaging(PackagingType.EAR)
@Extension("javax.enterprise.inject.spi.Extension")
public class AnnotatedTypeExtensionTest extends AbstractWeldTest
{
   
   @Test
   public void testMultipleBeansOfSameType()
   {
      Laundry laundry = getReference(Laundry.class);
      assert laundry.ecoFriendlyWashingMachine != null;
      assert laundry.fastWashingMachine != null;
   }
   

}
