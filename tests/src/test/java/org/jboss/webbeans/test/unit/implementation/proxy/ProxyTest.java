package org.jboss.webbeans.test.unit.implementation.proxy;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class ProxyTest extends AbstractWebBeansTest
{
   
   @Test(description="WBRI-122")
   public void testImplementationClassImplementsSerializable()
   {
      manager.getInstanceByName("foo");
   }
   
}
