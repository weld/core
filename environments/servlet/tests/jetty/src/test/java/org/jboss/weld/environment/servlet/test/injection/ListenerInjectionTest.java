package org.jboss.weld.environment.servlet.test.injection;

import static org.jboss.arquillian.api.RunModeType.AS_CLIENT;
import static org.jboss.weld.environment.servlet.test.util.JettyDeployments.JETTY_ENV;
import static org.jboss.weld.environment.servlet.test.util.JettyDeployments.JETTY_WEB;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.injection.ListenerInjectionTestBase;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Run(AS_CLIENT)
public class ListenerInjectionTest extends ListenerInjectionTestBase
{
   
   @Deployment
   public static WebArchive deployment()
   {
      return ListenerInjectionTestBase.deployment().addWebResource(JETTY_ENV, "jetty-env.xml").addWebResource(JETTY_WEB, "jetty-web.xml");
   }

}
