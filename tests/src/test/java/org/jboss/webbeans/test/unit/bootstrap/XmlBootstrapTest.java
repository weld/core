package org.jboss.webbeans.test.unit.bootstrap;

import javax.inject.Production;
import javax.inject.Standard;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.webbeans.WebBean;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
@BeansXml("test-beans.xml")
public class XmlBootstrapTest extends AbstractWebBeansTest
{
   
   @Test
   public void testDeploymentTypesLoadedFromBeansXml()
   {
      assert getCurrentManager().getEnabledDeploymentTypes().size() == 4;
      assert getCurrentManager().getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert getCurrentManager().getEnabledDeploymentTypes().get(1).equals(WebBean.class);
      assert getCurrentManager().getEnabledDeploymentTypes().get(2).equals(Production.class);
      assert getCurrentManager().getEnabledDeploymentTypes().get(3).equals(AnotherDeploymentType.class);
   }
   
}
