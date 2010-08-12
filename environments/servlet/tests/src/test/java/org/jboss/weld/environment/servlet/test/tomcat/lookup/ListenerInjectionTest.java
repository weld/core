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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
@RunWith(Arquillian.class)
@Run(AS_CLIENT)
public class ListenerInjectionTest
{
   
   public static final Asset WEB_XML = new ByteArrayAsset(("<web-app> <listener><listener-class>org.jboss.weld.environment.servlet.Listener</listener-class></listener> <listener><listener-class>" + BatListener.class.getName() + "</listener-class></listener> <servlet><servlet-name>Bat Servlet</servlet-name><servlet-class>org.jboss.weld.test.tomcat.lookup.BatServlet</servlet-class></servlet> <servlet-mapping><servlet-name>Bat Servlet</servlet-name><url-pattern>/bat</url-pattern></servlet-mapping> </web-app>").getBytes());
   
   @Deployment
   public static WebArchive deployment()
   {
      return DeploymentDescriptor.deployment(WEB_XML).addClasses(BatListener.class, BatServlet.class, Sewer.class);
   }
   
   @Test @Ignore
   // Injection doesn't work in listeners in Tomcat
   public void testListenerInjection() throws Exception
   {
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(CONTEXT_PATH + "/bat");
      int sc = client.executeMethod(method);
      assert sc == HttpServletResponse.SC_OK;
   }
}
