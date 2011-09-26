package org.jboss.weld.environment.servlet.test.injection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.net.URL;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.jboss.weld.environment.servlet.test.util.Deployments.extendDefaultWebXml;
import static org.junit.Assert.assertEquals;

public class ListenerInjectionTestBase {

    public static final Asset WEB_XML = new ByteArrayAsset(extendDefaultWebXml("<listener><listener-class>" + BatListener.class.getName() + "</listener-class></listener> <servlet><servlet-name>Bat Servlet</servlet-name><servlet-class>" + BatServlet.class.getName() + "</servlet-class></servlet> <servlet-mapping><servlet-name>Bat Servlet</servlet-name><url-pattern>/bat</url-pattern></servlet-mapping>").getBytes());

    public static WebArchive deployment() {
        return baseDeployment(WEB_XML).addClasses(BatListener.class, BatServlet.class, Sewer.class);
    }

    @Test
    @Ignore
    // Injection doesn't work in listeners in Tomcat
    public void testListenerInjection(@ArquillianResource URL baseURL) throws Exception {
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(new URL(baseURL, "bat").toExternalForm());
        int sc = client.executeMethod(method);
        assertEquals(HttpServletResponse.SC_OK, sc);
    }
}
