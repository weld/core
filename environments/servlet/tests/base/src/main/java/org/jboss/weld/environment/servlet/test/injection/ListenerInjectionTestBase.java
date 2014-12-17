package org.jboss.weld.environment.servlet.test.injection;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.jboss.weld.environment.servlet.test.util.Deployments.extendDefaultWebXml;
import static org.jboss.weld.environment.servlet.test.util.Deployments.toListener;
import static org.junit.Assert.assertEquals;

import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

public class ListenerInjectionTestBase {

    public static WebArchive deployment() {
        StringBuilder listeners = new StringBuilder();
        listeners.append(toListener(BatRequestListener.class.getName()));
        listeners.append(toListener(BatSessionListener.class.getName()));
        listeners.append(toListener(BatServletContextListener.class.getName()));
        Asset webXml = new ByteArrayAsset(
                extendDefaultWebXml(
                        listeners
                                + "<servlet><servlet-name>Bat Servlet</servlet-name><servlet-class>"
                                + BatServlet.class.getName()
                                + "</servlet-class></servlet> <servlet-mapping><servlet-name>Bat Servlet</servlet-name><url-pattern>/bat</url-pattern></servlet-mapping>")
                        .getBytes());
        return baseDeployment(webXml).addClasses(BatRequestListener.class, BatSessionListener.class, BatServletContextListener.class, BatListener.class, BatServlet.class, Sewer.class);
    }

    @Test
    public void testRequestListenerInjection(@ArquillianResource URL baseURL) throws Exception {
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(new URL(baseURL, "bat?mode=request").toExternalForm());
        int sc = client.executeMethod(method);
        assertEquals(HttpServletResponse.SC_OK, sc);
    }

    @Test
    public void testSceListenerInjection(@ArquillianResource URL baseURL) throws Exception {
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(new URL(baseURL, "bat?mode=sce").toExternalForm());
        int sc = client.executeMethod(method);
        assertEquals(HttpServletResponse.SC_OK, sc);
    }

    @Test
    public void testSessionListenerInjection(@ArquillianResource URL baseURL) throws Exception {
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(new URL(baseURL, "bat?mode=session").toExternalForm());
        int sc = client.executeMethod(method);
        assertEquals(HttpServletResponse.SC_OK, sc);
    }
}
