package org.jboss.weld.tests.servlet.dispatch;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Tests a combination of dispatch types (include/forward) and local/cross-context dispatch to verify that contexts are activated/deactivated properly. Each
 * dispatch request is followed by a validation request in which the number of constructions/destructions is examined to verify that an activated context is
 * always deactivated properly (no leaks occur).
 *
 * @author Jozef Hartinger
 * @author Ron Smeral
 *
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class DispatchingTest {

    @Deployment(testable = false)
    public static Archive<?> forwardingDeployment() {
        return Deployments.deployment(MainServlet.class);
    }

    @ArquillianResource(MainServlet.class)
    private URL contextPath;

    private final WebClient client = new WebClient();

    @Before
    public void reset() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        client.getPage(contextPath + "main/reset");
    }

    @Test
    public void testLocalInclude() throws IOException {
        assertEquals("first", getResponseAsString("main/dispatch/include"));
        assertEquals("true", getResponseAsString("main/validate"));
    }

    @Test
    public void testLocalForward(@ArquillianResource(MainServlet.class) URL contextPath) throws IOException {
        assertEquals("first", getResponseAsString("main/dispatch/forward"));
        assertEquals("true", getResponseAsString("main/validate"));
    }

    @Test
    public void testCrossContextInclude(@ArquillianResource(MainServlet.class) URL contextPath) throws IOException {
        assertEquals("second", getResponseAsString("main/dispatch/include?crossContext=true"));
        assertEquals("true", getResponseAsString("main/validate"));
    }

    @Test
    public void testCrossContextForward(@ArquillianResource(MainServlet.class) URL contextPath) throws IOException {
        assertEquals("second", getResponseAsString("main/dispatch/forward?crossContext=true"));
        assertEquals("true", getResponseAsString("main/validate"));
    }

    private String getResponseAsString(String urlSuffix) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        Page page = client.getPage(contextPath + urlSuffix);
        assertEquals(200, page.getWebResponse().getStatusCode());
        return page.getWebResponse().getContentAsString();
    }
}
