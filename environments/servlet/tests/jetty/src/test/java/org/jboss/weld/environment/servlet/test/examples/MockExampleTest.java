package org.jboss.weld.environment.servlet.test.examples;

import static org.jboss.weld.environment.servlet.test.util.JettyDeployments.JETTY_ENV;
import static org.jboss.weld.environment.servlet.test.util.JettyDeployments.JETTY_WEB;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MockExampleTest extends MockExampleTestBase
{
   
   @Deployment
   public static WebArchive deployment()
   {
      return MockExampleTestBase.deployment().addAsWebInfResource(JETTY_ENV, "jetty-env.xml").addAsWebInfResource(JETTY_WEB, "jetty-web.xml");
   }
   
}
