package org.jboss.weld.tests.extensions;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.jsr299.Extension;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@Extension("javax.enterprise.inject.spi.Extension")
public class ExtensionTest extends AbstractWeldTest
{
   
   @Test(description="WELD-234")
   public void testExtensionInjectableAsBean()
   {
      assert SimpleExtension.getInstance() != null;
      assert getCurrentManager().getInstanceByType(SimpleExtension.class).equals(SimpleExtension.getInstance());
   }

}
