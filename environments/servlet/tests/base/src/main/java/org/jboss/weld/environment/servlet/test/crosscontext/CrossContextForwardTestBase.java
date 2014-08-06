package org.jboss.weld.environment.servlet.test.crosscontext;

import static org.jboss.weld.environment.servlet.test.util.Deployments.extendDefaultWebXml;
import static org.jboss.weld.environment.servlet.test.util.Deployments.toContextParam;
import static org.jboss.weld.environment.servlet.test.util.Deployments.toServletAndMapping;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;


public class CrossContextForwardTestBase {

    protected static final String FIRST = "first";
    protected static final String SECOND = "second";
    protected static final Asset FORWARDING_WEB_XML = new ByteArrayAsset(extendDefaultWebXml(
            toServletAndMapping("Forwarding Servlet", ForwardingServlet.class, "/forwarding") + toContextParam("WELD_CONTEXT_ID_KEY", FIRST))
            .getBytes());
    protected static final Asset INCLUDED_WEB_XML = new ByteArrayAsset(extendDefaultWebXml(
            toServletAndMapping("Included Servlet", IncludedServlet.class, "/included") + toContextParam("WELD_CONTEXT_ID_KEY", SECOND)).getBytes());

    public static WebArchive createFirstTestArchive() {
         WebArchive war = ShrinkWrap.create(WebArchive.class,"app1.war").addAsWebInfResource(new BeansXml(), "beans.xml").setWebXML(FORWARDING_WEB_XML);
         war.addClass(ForwardingServlet.class);
         return war;
    }

    public static WebArchive createSecondTestArchive() {
        WebArchive war = ShrinkWrap.create(WebArchive.class,"app2.war").addAsWebInfResource(new BeansXml(), "beans.xml").setWebXML(INCLUDED_WEB_XML);
        war.addClass(IncludedServlet.class);
        return war;
    }


    @Test
    public void testCrossContextForward(@ArquillianResource @OperateOnDeployment(FIRST) URL firstContext) throws IOException {
      Page page = new WebClient().getPage(firstContext + "forwarding");
      assertEquals(200, page.getWebResponse().getStatusCode());
      assertEquals("<h1>Hello World</h1>", page.getWebResponse().getContentAsString());
    }
}