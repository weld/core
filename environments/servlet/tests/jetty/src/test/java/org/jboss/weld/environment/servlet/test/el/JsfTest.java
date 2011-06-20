package org.jboss.weld.environment.servlet.test.el;

import static org.jboss.weld.environment.servlet.test.util.JettyDeployments.JETTY_ENV;
import static org.jboss.weld.environment.servlet.test.util.JettyDeployments.JETTY_WEB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

@RunAsClient
@RunWith(Arquillian.class)
public class JsfTest extends JsfTestBase
{

   @Deployment
   public static WebArchive deployment()
   {
      WebArchive archive = JsfTestBase.deployment()
         .addAsWebResource(JETTY_ENV, "jetty-env.xml")
         .addAsWebResource(JETTY_WEB, "jetty-web.xml");
      return archive;
   }
   
   @Override
   protected String getPath(String page)
   {
      return "http://localhost:8888/test/" + page;
   }
   
}
