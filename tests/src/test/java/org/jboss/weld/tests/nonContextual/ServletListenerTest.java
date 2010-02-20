package org.jboss.weld.tests.nonContextual;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.war.WebXml;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@WebXml("web.xml")
public class ServletListenerTest extends AbstractWeldTest
{
   
   @Test(description="WELD-445")
   public void test()
   {
      assert ServletContextListenerImpl.ok;
   }

}
