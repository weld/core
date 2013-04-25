package org.jboss.weld.tests.servlet.crosscontext;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import java.io.IOException;
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class CrossContextInclusionTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(EnterpriseArchive.class)
                .addAsModule(ShrinkWrap.create(BeanArchive.class)
                    .addClass(CrossContextInclusionTest.class))
                .addAsModule(ShrinkWrap.create(WebArchive.class, "app1.war")
                    .addClass(MainServlet.class)
                    .addAsManifestResource(new StringAsset(""), "beans.xml"))
                .addAsModule(ShrinkWrap.create(WebArchive.class, "app2.war")
                    .addClass(IncludedServlet.class)
                    .addAsManifestResource(new StringAsset(""), "beans.xml"));
    }
    
    @Test
//    @Ignore("WELD-1415")
    public void testCrossContextInclusion(@ArquillianResource(MainServlet.class) URL servlet) throws IOException {
        WebClient client = new WebClient();
        Page page = client.getPage(servlet);
        assertEquals(200, page.getWebResponse().getStatusCode());
        assertEquals("<h1>Hello World</h1>", page.getWebResponse().getContentAsString());
    }
}
