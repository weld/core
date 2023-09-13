package org.jboss.weld.environment.servlet.test.context.async;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * @author Martin Kouba
 * @author Tomas Remes
 */
@RunAsClient
@RunWith(Arquillian.class)
public class SimpleAsyncListenerTest {

    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static WebArchive createTestArchive() {
        return baseDeployment().addClasses(SimpleAsyncListenerTest.class, FailingServlet.class, StatusServlet.class,
                AsyncServlet.class, AsyncRequestProcessor.class, SimpleAsyncListener.class);
    }

    @Test
    public void testOnCompleteCalledSuccesfully() throws Exception {
        WebClient webClient = new WebClient();
        webClient.getPage(getPath(AsyncServlet.TEST_COMPLETE));
        Page results = webClient.getPage(contextPath + "Status");
        assertTrue(results.getWebResponse().getContentAsString().contains("onComplete: true"));
    }

    @Test
    public void testOnTimeoutCalledSuccesfully() throws Exception {
        WebClient webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getPage(getPath(AsyncServlet.TEST_TIMEOUT));
        Page results = webClient.getPage(contextPath + "Status");
        assertTrue(results.getWebResponse().getContentAsString().contains("onTimeout: true"));
    }

    @Test
    @Ignore //enable when WELD-1774 is fixed
    public void testOnErrorCalledSuccesfully() throws Exception {
        WebClient webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getPage(getPath(AsyncServlet.TEST_ERROR));
        Page results = webClient.getPage(contextPath + "Status");
        assertTrue(results.getWebResponse().getContentAsString(),
                results.getWebResponse().getContentAsString().contains("onError: true"));
    }

    @Test
    public void testOnStartAsyncCalledSuccesfully() throws Exception {
        WebClient webClient = new WebClient();
        webClient.getPage(getPath(AsyncServlet.TEST_LOOP));
        Page results = webClient.getPage(contextPath + "Status");
        assertTrue(results.getWebResponse().getContentAsString().contains("onComplete: true"));
        assertTrue(results.getWebResponse().getContentAsString().contains("onStartAsync: true"));
    }

    private String getPath(String test) {
        return contextPath + "AsyncServlet?test=" + test;
    }

}
