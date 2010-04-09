package org.jboss.weld.tests.proxy.weld56;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@Packaging(PackagingType.WAR)
@Resource(source = "org.jboss.weld.enableUnsafeProxies", destination = "WEB-INF/classes/META-INF/org.jboss.weld.enableUnsafeProxies")
public class ProxyTest extends AbstractWeldTest
{

   @Test
   public void testProxy()
   {
      assert "ping".equals(getReference(Foo.class).ping());
   }
}
