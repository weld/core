package org.jboss.weld.environment.servlet.test.bootstrap;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.servlet.ServletContextEvent;

import static org.jboss.weld.environment.servlet.test.bootstrap.EventHolder.events;
import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.jboss.weld.environment.servlet.test.util.Deployments.extendDefaultWebXml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BootstrapOrderingTestBase
{

   public static final Asset WEB_XML = new ByteArrayAsset(extendDefaultWebXml("<listener><listener-class>" + MyServletContextListener.class.getName() + "</listener-class></listener>").getBytes());
   public static final Asset EXTENSION = new ByteArrayAsset(MyExtension.class.getName().getBytes());

   public static WebArchive deployment()
   {
      return baseDeployment(WEB_XML).addPackage(BootstrapOrderingTestBase.class.getPackage()).addAsWebInfResource(EXTENSION, "classes/META-INF/services/" + Extension.class.getName());
   }

   @Test
   public void testContextInitializedCalledBeforeBeanValidation()
   {
      assertEquals(4, events.size());
      assertTrue(events.get(0) instanceof BeforeBeanDiscovery);
      assertTrue(events.get(1) instanceof AfterBeanDiscovery);
      assertTrue(events.get(2) instanceof AfterDeploymentValidation);
      assertTrue(events.get(3) instanceof ServletContextEvent);
   }

}
