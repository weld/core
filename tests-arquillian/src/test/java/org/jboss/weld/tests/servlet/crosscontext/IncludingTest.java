package org.jboss.weld.tests.servlet.crosscontext;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class IncludingTest {

    @Deployment(testable = false)
    public static Archive<?> includingDeployment() {
        return Deployments.deployment(IncludingServlet.class);
    }

    @Test
    public void testInclude(@ArquillianResource(IncludingServlet.class) URL contextPath) throws IOException {
        Page page = new WebClient().getPage(contextPath + "including");
        assertEquals(200, page.getWebResponse().getStatusCode());
        assertEquals("<h1>Hello World</h1>", page.getWebResponse().getContentAsString());
    }
}
