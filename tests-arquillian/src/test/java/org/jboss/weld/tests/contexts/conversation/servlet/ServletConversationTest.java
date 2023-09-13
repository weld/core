/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.contexts.conversation.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.jboss.weld.test.util.Utils;
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
        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(ServletConversationTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(Message.class, Servlet.class, DestroyedConversationObserver.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
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
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
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

        // Do a redirect. Verify that the conversation is not propagated (In this case, the application must manage this request
        // parameter.)
        TextPage page = client.getPage(getPath("/redirect", cid));
        assertTrue(page.getContent().contains("message: Hello"));
        assertTrue(page.getContent().contains("cid: [null]"));
        assertTrue(page.getContent().contains("transient: true"));
    }

    @Test
    public void testInvalidatingSessionDestroysConversation() throws Exception {
        WebClient client = new WebClient();

        // begin conversation 1
        TextPage initialPage1 = client.getPage(getPath("/begin", null));
        String content = initialPage1.getContent();
        assertTrue(content.contains("message: Hello"));
        assertTrue(content.contains("transient: false"));
        String cid1 = getCid(content);

        // begin conversation 1
        TextPage initialPage2 = client.getPage(getPath("/begin", null));
        String content2 = initialPage2.getContent();
        assertTrue(content2.contains("message: Hello"));
        assertTrue(content2.contains("transient: false"));
        String cid2 = getCid(content2);

        assertFalse(cid1.equals(cid2));

        /*
         * Invalidate the session. This should destroy the currently associated conversation (with cid1) as well as the
         * not-currently-associated conversation (with cid2).
         */
        {
            client.getPage(getPath("/invalidateSession", cid1));
        }

        // verify destroyed conversations
        {
            TextPage page = client.getPage(getPath("/listDestroyedMessages", null));
            assertTrue(page.getContent().contains("DestroyedMessages:"));
            assertTrue(page.getContent().contains("<M:" + cid1 + ">"));
            assertTrue(page.getContent().contains("<M:" + cid2 + ">"));
        }
        {
            TextPage page = client.getPage(getPath("/listConversationsDestroyedWhileBeingAssociated", null));
            assertTrue(page.getContent().contains("ConversationsDestroyedWhileBeingAssociated:"));
            assertTrue(page.getContent().contains("<" + cid1 + ">"));
        }
        {
            TextPage page = client.getPage(getPath("/listConversationsDestroyedWhileBeingDisassociated", null));
            assertTrue(page.getContent().contains("ConversationsDestroyedWhileBeingDisassociated:"));
            assertTrue(page.getContent().contains("<" + cid2 + ">"));
        }

        // Verify that the conversation 1 cannot be associated
        {
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
            Page page = client.getPage(getPath("/display", cid1));
            assertEquals(500, page.getWebResponse().getStatusCode());
        }

        // Verify that the conversation 2 cannot be associated
        {
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
            Page page = client.getPage(getPath("/display", cid2));
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

        for (HtmlElement element : rootElement.getHtmlElementDescendants()) {
            result.addAll(getElements(element, elementClass));
        }

        if (elementClass.isInstance(rootElement)) {
            result.add(elementClass.cast(rootElement));
        }
        return result;

    }
}
