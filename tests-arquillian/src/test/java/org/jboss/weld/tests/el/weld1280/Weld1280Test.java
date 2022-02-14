package org.jboss.weld.tests.el.weld1280;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 *
 * @author tremes
 *
 */

@Category(Integration.class)
@RunWith(Arquillian.class)
public class Weld1280Test {

    @ArquillianResource
    URL url;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class, Utils.getDeploymentNameAsHash(Weld1280Test.class, Utils.ARCHIVE_TYPE.WAR)).addClasses(WeldTestPhaseListener.class, HelloBean.class)
                .addAsWebResource(Weld1280Test.class.getPackage(), "index.xhtml", "index.xhtml")
                .addAsWebInfResource(Weld1280Test.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(Weld1280Test.class.getPackage(), "faces-config.xml", "faces-config.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

    }

    @Test
    public void testELContextOfDepedentScopeBean() throws Exception {
        WebClient client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage main = client.getPage(url);
        assertTrue(main.getBody().asNormalizedText().contains("Hello from dependent scope bean"));
    }

}
