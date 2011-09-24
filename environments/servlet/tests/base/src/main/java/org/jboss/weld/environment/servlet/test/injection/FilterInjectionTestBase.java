package org.jboss.weld.environment.servlet.test.injection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.net.URL;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.jboss.weld.environment.servlet.test.util.Deployments.extendDefaultWebXml;
import static org.junit.Assert.assertEquals;

public class FilterInjectionTestBase {

    public static final Asset WEB_XML = new ByteArrayAsset(extendDefaultWebXml("<filter><filter-name>Cat Filter</filter-name><filter-class>" + CatFilter.class.getName() + "</filter-class></filter><filter-mapping><filter-name>Cat Filter</filter-name><url-pattern>/cat</url-pattern></filter-mapping> <servlet><servlet-name>Wolverine Servlet</servlet-name><servlet-class>" + WolverineServlet.class.getName() + "</servlet-class></servlet> <servlet-mapping><servlet-name>Wolverine Servlet</servlet-name><url-pattern>/</url-pattern></servlet-mapping>").getBytes());

    public static WebArchive deployment() {
        return baseDeployment(WEB_XML).addClasses(CatFilter.class, Sewer.class, RatServlet.class);
    }

    @Test
    public void testFilterInjection(@ArquillianResource URL baseURL) throws Exception {
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(new URL(baseURL, "cat").toExternalForm());
        int sc = client.executeMethod(method);
        assertEquals(HttpServletResponse.SC_OK, sc);
    }
}
