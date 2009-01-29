package org.jboss.webbeans.test.unit.implementation.proxy;

import org.jboss.webbeans.test.unit.AbstractTest;
import org.testng.annotations.Test;

public class ProxyTest extends AbstractTest
{
   
   @Test(description="WBRI-122")
   public void testImplementationClassImplementsSerializable()
   {
      deployBeans(Foo.class);
      manager.getInstanceByName("foo");
   }
   
}
