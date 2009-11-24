package org.jboss.weld.environment.servlet.discovery;

import java.net.URL;
import java.util.Set;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.environment.servlet.deployment.WebAppBeanDeploymentArchive;
import org.jboss.weld.mock.MockServletContext;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

/**
 * A set of tests to verify the discovery of JSR-299 resources in a servlet environment.
 *
 * @author Dan Allen
 */
@Artifact
// for now we are just going to look for the one in test-classes/META-INF/beans.xml using standalone mode
//@Resources(
//   @Resource(source = "beans.xml", destination = "WEB-INF/classes/META-INF/beans.xml")
//)
public class ServletWeldDiscoveryTest extends AbstractWeldTest
{
   /**
    * Verify that the scanner properly locates any META-INF/beans.xml. To prevent a brittle test,
    * assume that there is at least one found and that the path ends with META-INF/beans.xml. Technically,
    * META-INF would need to be the root of the classpath.
    */
   @Test
   public void testDiscoverMetaInfBeansXml()
   {
      WebAppBeanDeploymentArchive discovery = new WebAppBeanDeploymentArchive(new MockServletContext("."))
      {
      };

      Set<URL> beansXmls = discovery.getWeldUrls();
      assert beansXmls.size() >= 1 : "Expecting to find at least one META-INF/beans.xml resource on the test classpath";
      for (URL beansXml : beansXmls)
      {
         assert beansXml.getPath().endsWith("META-INF/beans.xml") : beansXml.getPath() + " does not end with META-INF/beans.xml as expected";
      }
   }
}
