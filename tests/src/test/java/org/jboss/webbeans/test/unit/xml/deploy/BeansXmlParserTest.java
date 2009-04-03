package org.jboss.webbeans.test.unit.xml.deploy;

import java.net.URL;
import java.util.Iterator;

import javax.inject.Production;
import javax.inject.Standard;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.mock.MockResourceLoader;
import org.jboss.webbeans.mock.MockXmlEnvironment;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.XmlParser;
import org.testng.annotations.Test;

@Artifact
@Resources({
   @Resource(destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/deploy/default-beans.xml", source="/org/jboss/webbeans/test/unit/xml/deploy/default-beans.xml"),
   @Resource(destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/deploy/duplicate-deployments-beans.xml", source="/org/jboss/webbeans/test/unit/xml/deploy/duplicate-deployments-beans.xml"),
   @Resource(destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/deploy/user-defined-beans.xml", source="/org/jboss/webbeans/test/unit/xml/deploy/user-defined-beans.xml")
})
@Classes(packages="org.jboss.webbeans.test.unit.xml.beans")
public class BeansXmlParserTest extends AbstractWebBeansTest
{
   
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
      XmlEnvironment environment = new MockXmlEnvironment(urls, new EjbDescriptorCache());
      XmlParser parser = new XmlParser(environment);
      parser.parse();
      
      assert environment.getEnabledDeploymentTypes().size() == 2;
      assert environment.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert environment.getEnabledDeploymentTypes().get(1).equals(Production.class);
   }
   
   @Test
   public void testUserDefinedDeploymentType()
   {
      Iterable<URL> urls = getResources("user-defined-beans.xml");
      XmlEnvironment environment = new MockXmlEnvironment(urls, new EjbDescriptorCache());
      XmlParser parser = new XmlParser(environment);
      parser.parse();
      assert environment.getEnabledDeploymentTypes().size() == 3;
      assert environment.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert environment.getEnabledDeploymentTypes().get(1).equals(Production.class);
      assert environment.getEnabledDeploymentTypes().get(2).equals(AnotherDeploymentType.class);
   }
   
   /**
    * Test case for WBRI-21.
    */
   //@Test(expectedExceptions=DeploymentException.class, description="WBRI-21")
   public void testDuplicateDeployElement()
   {
      Iterable<URL> urls = getResources("duplicate-deployments-beans.xml");
      XmlEnvironment environment = new MockXmlEnvironment(urls, new EjbDescriptorCache());
      XmlParser parser = new XmlParser(environment);
      parser.parse();
   }
   
}
