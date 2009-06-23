package org.jboss.webbeans.test.unit.xml.deploy;

import java.net.URL;
import java.util.Iterator;

import javax.enterprise.inject.deployment.Production;
import javax.enterprise.inject.deployment.Standard;
import javax.inject.DeploymentException;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.webbeans.mock.MockResourceLoader;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.jboss.webbeans.xml.BeansXmlParser;
import org.testng.annotations.Test;

@Artifact
@Resources({
   @Resource(destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/deploy/default-beans.xml", source="/org/jboss/webbeans/test/unit/xml/deploy/default-beans.xml"),
   @Resource(destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/deploy/duplicate-deployments-beans.xml", source="/org/jboss/webbeans/test/unit/xml/deploy/duplicate-deployments-beans.xml"),
   @Resource(destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/deploy/user-defined-beans.xml", source="/org/jboss/webbeans/test/unit/xml/deploy/user-defined-beans.xml")
})
@Classes(
      packages={"org.jboss.webbeans.test.unit.xml.beans", "org.jboss.webbeans.test.unit.xml.beans.annotationtype"}
)
public class BeansXmlParserTest extends AbstractWebBeansTest
{

   // Quick unit tests for the parser
   @Test(groups="incontainer-broken")
   public void testDefaultDeploymentTypes()
   {
      Iterable<URL> urls = getResources("default-beans.xml");
      int i = 0;
      Iterator<URL> it = urls.iterator();
      while (it.hasNext())
      {
         i++;
         it.next();
      }
      assert i == 1;
      BeansXmlParser parser = new BeansXmlParser(new MockResourceLoader(), urls);
      parser.parse();

      assert parser.getEnabledDeploymentTypes().size() == 2;
      assert parser.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert parser.getEnabledDeploymentTypes().get(1).equals(Production.class);
   }

   @Test(groups="incontainer-broken")
   public void testUserDefinedDeploymentType()
   {
      Iterable<URL> urls = getResources("user-defined-beans.xml");
      BeansXmlParser parser = new BeansXmlParser(new MockResourceLoader(), urls);
      parser.parse();
      assert parser.getEnabledDeploymentTypes().size() == 3;
      assert parser.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert parser.getEnabledDeploymentTypes().get(1).equals(Production.class);
      assert parser.getEnabledDeploymentTypes().get(2).equals(AnotherDeploymentType.class);
   }

   /**
    * Test case for WBRI-21.
    */
   @Test(expectedExceptions=DeploymentException.class, description="WBRI-21", groups="incontainer-broken")
   public void testDuplicateDeployElement()
   {
      Iterable<URL> urls = getResources("duplicate-deployments-beans.xml");
      BeansXmlParser parser = new BeansXmlParser(new MockResourceLoader(), urls);
      parser.parse();
   }

}
