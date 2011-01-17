package org.jboss.weld.environment.servlet.test.injection;

import static org.jboss.weld.environment.servlet.test.util.Deployments.CONTEXT_PATH;
import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.jboss.weld.environment.servlet.test.util.Deployments.extendDefaultWebXml;
import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

public class ServletInjectionTestBase
{

   public static final Asset WEB_XML = new ByteArrayAsset(extendDefaultWebXml("<servlet><servlet-name>Rat Servlet</servlet-name><servlet-class>" + RatServlet.class.getName() + "</servlet-class></servlet> <servlet-mapping><servlet-name>Rat Servlet</servlet-name><url-pattern>/rat</url-pattern></servlet-mapping>").getBytes());

   public static WebArchive deployment()
   {
      return baseDeployment(WEB_XML).addClasses(RatServlet.class, Sewer.class);
   }

   @Test
   public void testServletInjection() throws Exception
   {
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(CONTEXT_PATH + "/rat");
      assertEquals(client.executeMethod(method), HttpServletResponse.SC_OK);
   }

}
