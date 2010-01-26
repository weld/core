package org.jboss.weld.tests.resources;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
public class ResourceTest extends AbstractWeldTest
{
   
   @Test(description="WELD-385")
   public void testUTInjectedByResource()
   {
      assert getReference(UTConsumer.class).getUserTransaction() != null;
   }
   

}
