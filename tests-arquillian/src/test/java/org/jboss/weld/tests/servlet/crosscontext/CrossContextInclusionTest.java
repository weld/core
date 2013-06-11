package org.jboss.weld.tests.servlet.crosscontext;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class CrossContextInclusionTest {

    @Deployment(testable = false)
    public static Archive<?> deployment() {

        WebArchive war1 = ShrinkWrap.create(WebArchive.class, "app1.war")
                .addClass(MainServlet.class)
                .addAsWebInfResource(new StringAsset(""), "beans.xml");

        WebArchive war2 = ShrinkWrap.create(WebArchive.class, "app2.war")
                .addClass(IncludedServlet.class)
                .addAsWebInfResource(new StringAsset(""), "beans.xml");

        return ShrinkWrap.create(EnterpriseArchive.class)
                .addAsModule(Testable.archiveToTest(war1))
                .addAsModule(war2);
    }

    @Test
    public void testCrossContextInclusion(@ArquillianResource(MainServlet.class) URL contextPath) throws IOException {
        Page page = new WebClient().getPage(contextPath + "/main");
        assertEquals(200, page.getWebResponse().getStatusCode());
        assertEquals("<h1>Hello World</h1>", page.getWebResponse().getContentAsString());
    }
}
