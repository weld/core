package org.jboss.weld.environment.servlet.test.tomcat.lookup;

import static org.jboss.arquillian.api.RunModeType.AS_CLIENT;
import static org.jboss.weld.environment.servlet.test.util.DeploymentDescriptor.CONTEXT_PATH;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.util.DeploymentDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Run(AS_CLIENT)
public class ServletInjectionTest
{

   public static final Asset WEB_XML = new ByteArrayAsset(("<web-app> <listener><listener-class>org.jboss.weld.environment.servlet.Listener</listener-class></listener> <servlet><servlet-name>Rat Servlet</servlet-name><servlet-class>" + RatServlet.class.getName() + "</servlet-class></servlet> <servlet-mapping><servlet-name>Rat Servlet</servlet-name><url-pattern>/rat</url-pattern></servlet-mapping> </web-app>").getBytes());

   @Deployment
   public static WebArchive deployment()
   {
      return DeploymentDescriptor.deployment(WEB_XML).addClasses(RatServlet.class, Sewer.class);
   }

   @Test
   public void testServletInjection() throws Exception
   {
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(CONTEXT_PATH + "/rat");
      assert client.executeMethod(method) == HttpServletResponse.SC_OK;
   }

}
