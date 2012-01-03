/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.contexts.conversation.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * 
 * @author Jozef Hartinger
 * 
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class ServletConversationTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war").addClasses(Message.class, Servlet.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebResource(ServletConversationTest.class.getPackage(), "message.html", "message.html");
    }

    @Test
    public void testTransientConversation() throws Exception {
        WebClient client = new WebClient();
        TextPage page = client.getPage(getPath("/display", null));
        assertTrue(page.getContent().contains("message: Hello"));
        assertTrue(page.getContent().contains("cid: [null]"));
        assertTrue(page.getContent().contains("transient: true"));
    }

    @Test
    public void testLongRunningConversation() throws Exception {
        WebClient client = new WebClient();

        // begin conversation
        TextPage initialPage = client.getPage(getPath("/begin", null));
        String content = initialPage.getContent();
        assertTrue(content.contains("message: Hello"));
        assertTrue(content.contains("transient: false"));

        String cid = getCid(content);

        // verify conversation is not transient
        {
            TextPage page = client.getPage(getPath("/display", cid));
            assertTrue(page.getContent().contains("message: Hello"));
            assertTrue(page.getContent().contains("cid: [" + cid + "]"));
            assertTrue(page.getContent().contains("transient: false"));
        }

        // modify conversation state
        {
            TextPage page = client.getPage(getPath("/set", cid) + "&message=Hi");
            assertTrue(page.getContent().contains("message: Hi"));
            assertTrue(page.getContent().contains("cid: [" + cid + "]"));
            assertTrue(page.getContent().contains("transient: false"));
        }

        // verify conversation state
        {
            TextPage page = client.getPage(getPath("/display", cid));
            assertTrue(page.getContent().contains("message: Hi"));
            assertTrue(page.getContent().contains("cid: [" + cid + "]"));
            assertTrue(page.getContent().contains("transient: false"));
        }

        // end conversation
        {
            TextPage page = client.getPage(getPath("/end", cid));
            assertTrue(page.getContent().contains("message: Hi"));
            assertTrue(page.getContent().contains("transient: true"));
        }

        // verify that the conversation can no longer be restored
        {
            client.setThrowExceptionOnFailingStatusCode(false);
            Page page = client.getPage(getPath("/display", cid));
            assertEquals(500, page.getWebResponse().getStatusCode());
        }
    }

    @Test
    public void testPost() throws Exception {
        WebClient client = new WebClient();

        // begin conversation
        TextPage initialPage = client.getPage(getPath("/begin", null));
        String content = initialPage.getContent();
        assertTrue(content.contains("message: Hello"));
        assertTrue(content.contains("transient: false"));

        String cid = getCid(content);

        // submit a form
        {
            HtmlPage form = client.getPage(url.toString() + "/message.html");
            getFirstMatchingElement(form, HtmlTextInput.class, "message").setValueAttribute("Hola!");
            getFirstMatchingElement(form, HtmlTextInput.class, "cid").setValueAttribute(cid);
            TextPage page = getFirstMatchingElement(form, HtmlSubmitInput.class, "submit").click();

            assertTrue(page.getContent().contains("message: Hola!"));
            assertTrue(page.getContent().contains("cid: [" + cid + "]"));
            assertTrue(page.getContent().contains("transient: false"));
        }

        // verify conversation state
        {
            TextPage page = client.getPage(getPath("/display", cid));
            assertTrue(page.getContent().contains("message: Hola!"));
            assertTrue(page.getContent().contains("cid: [" + cid + "]"));
            assertTrue(page.getContent().contains("transient: false"));
        }
    }

    @Test
    public void testRedirect() throws Exception {
        WebClient client = new WebClient();

        // begin conversation
        TextPage initialPage = client.getPage(getPath("/begin", null));
        String content = initialPage.getContent();
        assertTrue(content.contains("message: Hello"));
        assertTrue(content.contains("transient: false"));

        String cid = getCid(content);

        // Do a redirect. Verify that the conversation is not propagated (In this case, the application must manage this request parameter.)
        TextPage page = client.getPage(getPath("/redirect", cid));
        assertTrue(page.getContent().contains("message: Hello"));
        assertTrue(page.getContent().contains("cid: [null]"));
        assertTrue(page.getContent().contains("transient: true"));
    }
    
    @Test
    public void testInvalidatingSessionDestroysConversation() throws Exception {
        WebClient client = new WebClient();
        
        // begin conversation
        TextPage initialPage = client.getPage(getPath("/begin", null));
        String content = initialPage.getContent();
        assertTrue(content.contains("message: Hello"));
        assertTrue(content.contains("transient: false"));

        String cid = getCid(content);

        // Invalidate the session
        {
            client.getPage(getPath("/invalidateSession", cid));
        }

        // Verify that the conversation cannot be associated
        {
            client.setThrowExceptionOnFailingStatusCode(false);
            Page page = client.getPage(getPath("/display", cid));
            assertEquals(500, page.getWebResponse().getStatusCode());
        }
    }

    protected String getCid(String content) {
        return content.substring(content.indexOf("cid: [") + 6, content.indexOf("]"));
    }

    protected String getPath(String viewId, String cid) {
        StringBuilder builder = new StringBuilder(url.toString());
        builder.append("/servlet");
        builder.append(viewId);
        if (cid != null) {
            builder.append("?");
            builder.append("cid");
            builder.append("=");
            builder.append(cid);
        }
        return builder.toString();
    }

    protected <T extends HtmlElement> T getFirstMatchingElement(HtmlPage page, Class<T> elementClass, String id) {

        Set<T> inputs = getElements(page.getBody(), elementClass);
        for (T input : inputs) {
            if (input.getId().contains(id)) {
                return input;
            }
        }
        return null;
    }

    protected <T> Set<T> getElements(HtmlElement rootElement, Class<T> elementClass) {
        Set<T> result = new HashSet<T>();

        for (HtmlElement element : rootElement.getAllHtmlChildElements()) {
            result.addAll(getElements(element, elementClass));
        }

        if (elementClass.isInstance(rootElement)) {
            result.add(elementClass.cast(rootElement));
        }
        return result;

    }
}
