package org.jboss.weld.tests.proxy.weld56;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ProxyTest extends AbstractWeldTest
{

   @Test(groups="broken")
   public void testProxy()
   {
      assert "ping".equals(getReference(Foo.class).ping());
   }
}
