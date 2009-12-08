package org.jboss.weld.tests.enterprise.proxyability;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
@IntegrationTest
public class EnterpriseBeanLifecycleTest extends AbstractWeldTest
{

   @Test(description="WELD-290")
   public void testSLSBInjectedIntoPassivatingManagedBean()
   {
      SimpleBean bean = getCurrentManager().getInstanceByType(SimpleBean.class);
      assert bean.getMessage().equals("This is my message from my stateless bean");
      
   }
 
}
