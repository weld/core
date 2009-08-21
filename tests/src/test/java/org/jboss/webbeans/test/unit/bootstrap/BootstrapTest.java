package org.jboss.webbeans.test.unit.bootstrap;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
@IntegrationTest
@Resources({
   @Resource(source="javax.enterprise.inject.spi.Extension", destination="META-INF/services/javax.enterprise.inject.spi.Extension")
})
public class BootstrapTest extends AbstractWebBeansTest
{
   
   @Test(groups="bootstrap")
   public void testInitializedEvent()
   {
      assert InitializedObserver.observered;
   }
   
}
