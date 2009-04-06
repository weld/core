package org.jboss.webbeans.test.unit.implementation.enterprise;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@Packaging(PackagingType.EAR)
public class EnterpriseBeanTest extends AbstractWebBeansTest
{
   
   @Test(description="WBRI-179")
   public void testSFSBWithOnlyRemoteInterfacesDeploys()
   {
      
   }
   
   
   
}
