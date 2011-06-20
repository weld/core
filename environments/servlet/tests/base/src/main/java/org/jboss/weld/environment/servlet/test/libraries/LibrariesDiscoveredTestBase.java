package org.jboss.weld.environment.servlet.test.libraries;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.util.BeansXml;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.logging.Logger;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class LibrariesDiscoveredTestBase
{

   private static final Logger log = Logger.getLogger(LibrariesDiscoveredTestBase.class.getName());

   public static WebArchive deployment()
   {
      JavaArchive library = ShrinkWrap.create(JavaArchive.class, "library.jar").addClass(Camel.class).addAsManifestResource(new BeansXml(), "beans.xml");
      log.fine("-------------------------");
      log.fine("Library");
      log.fine("-------------------------");
      log.fine(library.toString(true));
      return baseDeployment().addClass(Needle.class).addAsLibrary(library);
   }

   @Test
   public void testCamelDeployed(Camel camel, Needle needle)
   {
      assertNotNull(camel);
      assertNotNull(needle);
   }

}
