package org.jboss.weld.test.unit.implementation.proxy.enterprise;

import javassist.util.proxy.ProxyObject;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class EnterpriseBeanProxyTest extends AbstractWeldTest
{
   
   /**
    * <a href="https://jira.jboss.org/jira/browse/WBRI-109">WBRI-109</a>
    */
   @Test(description="WBRI-109")
   public void testNoInterfaceView() throws Exception
   {
      Object mouse = getCurrentManager().getInstanceByType(MouseLocal.class);
      assert mouse instanceof ProxyObject;
      assert mouse instanceof MouseLocal;
   }
   
}
