package org.jboss.weld.environment.servlet.test.injection;

import static org.jboss.weld.environment.servlet.test.util.Deployments.CONTEXT_PATH;
import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

public class FilterInjectionTestBase
{
   
   public static final Asset WEB_XML = new ByteArrayAsset(("<web-app><listener><listener-class>org.jboss.weld.environment.servlet.Listener</listener-class></listener> <filter><filter-name>Cat Filter</filter-name><filter-class>" + CatFilter.class.getName() + "</filter-class></filter><filter-mapping><filter-name>Cat Filter</filter-name><url-pattern>/cat</url-pattern></filter-mapping> <servlet><servlet-name>Wolverine Servlet</servlet-name><servlet-class>" + WolverineServlet.class.getName() + "</servlet-class></servlet> <servlet-mapping><servlet-name>Wolverine Servlet</servlet-name><url-pattern>/</url-pattern></servlet-mapping></web-app>").getBytes());
   
   public static WebArchive deployment()
   {
      return baseDeployment(WEB_XML).addClasses(CatFilter.class, Sewer.class, RatServlet.class);
   }
   
   @Test
   public void testFilterInjection() throws Exception
   {
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(CONTEXT_PATH + "/cat");
      int sc = client.executeMethod(method);
      assert sc == HttpServletResponse.SC_OK;
   }
}
