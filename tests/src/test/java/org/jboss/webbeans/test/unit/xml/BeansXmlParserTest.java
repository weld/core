package org.jboss.webbeans.test.unit.xml;

import java.net.URL;
import java.util.Iterator;

import javax.inject.DeploymentException;
import javax.inject.Production;
import javax.inject.Standard;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.webbeans.bootstrap.BeansXmlParser;
import org.jboss.webbeans.mock.MockResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
@Resources({
   @Resource(destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/default-beans.xml", source="/org/jboss/webbeans/test/unit/xml/default-beans.xml"),
   @Resource(destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/duplicate-deployments-beans.xml", source="/org/jboss/webbeans/test/unit/xml/duplicate-deployments-beans.xml"),
   @Resource(destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/user-defined-beans.xml", source="/org/jboss/webbeans/test/unit/xml/user-defined-beans.xml")
})
public class BeansXmlParserTest extends AbstractWebBeansTest
{
   
   private static final ResourceLoader RESOURCE_LOADER = new MockResourceLoader(); 
   
   // Quick unit tests for the parser
   @Test
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
      BeansXmlParser parser = new BeansXmlParser(RESOURCE_LOADER, urls);
      parser.parse();
      assert parser.getEnabledDeploymentTypes().size() == 2;
      assert parser.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert parser.getEnabledDeploymentTypes().get(1).equals(Production.class);
   }
   
   @Test
   public void testUserDefinedDeploymentType()
   {
      Iterable<URL> urls = getResources("user-defined-beans.xml");
      BeansXmlParser parser = new BeansXmlParser(RESOURCE_LOADER, urls);
      parser.parse();
      assert parser.getEnabledDeploymentTypes().size() == 3;
      assert parser.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert parser.getEnabledDeploymentTypes().get(1).equals(Production.class);
      assert parser.getEnabledDeploymentTypes().get(2).equals(AnotherDeploymentType.class);
   }
   
   /**
    * Test case for WBRI-21.
    */
   @Test(expectedExceptions=DeploymentException.class, description="WBRI-21")
   public void testDuplicateDeployElement()
   {
      Iterable<URL> urls = getResources("duplicate-deployments-beans.xml");
      BeansXmlParser parser = new BeansXmlParser(RESOURCE_LOADER, urls);
      parser.parse();
   }
   
}
