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
package org.jboss.weld.tests.contexts.conversation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * @author Nicklas Karlsson
 * @author Dan Allen
 * @author Jozef Hartinger
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class ClientConversationContextTest {

    public static final String CID_REQUEST_PARAMETER_NAME = "cid";

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(ClientConversationContextTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(ConversationTestPhaseListener.class, Cloud.class, Thunderstorm.class, Hailstorm.class,
                        Hurricane.class, Snowstorm.class,
                        LockingIssueBean.class, Tornado.class, ExceptionWritingFilter.class)
                .addAsWebInfResource(ClientConversationContextTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ClientConversationContextTest.class.getPackage(), "faces-config.xml", "faces-config.xml")
                .addAsWebResource(ClientConversationContextTest.class.getPackage(), "cloud.xhtml", "cloud.xhtml")
                .addAsWebResource(ClientConversationContextTest.class.getPackage(), "tornado.xhtml", "tornado.xhtml")
                .addAsWebResource(ClientConversationContextTest.class.getPackage(), "thunderstorm.xhtml", "thunderstorm.xhtml")
                .addAsWebResource(ClientConversationContextTest.class.getPackage(), "snowstorm.xhtml",
                        "/winter/snowstorm.xhtml")
                .addAsWebResource(ClientConversationContextTest.class.getPackage(), "hailstorm.xhtml", "hailstorm.xhtml")
                .addAsWebResource(ClientConversationContextTest.class.getPackage(), "locking-issue.xhtml",
                        "locking-issue.xhtml")
                .addAsWebResource(ClientConversationContextTest.class.getPackage(), "blizzard.xhtml", "blizzard.xhtml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testConversationNotPropagatedByHLink() throws Exception {
        WebClient client = new WebClient();

        // Access the start page
        HtmlPage cloud = client.getPage(getPath("/cloud.xhtml"));
        String cloudName = getFirstMatchingElement(cloud, HtmlSpan.class, "cloudName").getTextContent();
        assertEquals(Cloud.NAME, cloudName);

        // Now start a conversation and check the cloud name changes
        HtmlPage blizzard = getFirstMatchingElement(cloud, HtmlSubmitInput.class, "blizzard").click();
        cloudName = getFirstMatchingElement(blizzard, HtmlSpan.class, "cloudName").getTextContent();
        assertEquals("henry", cloudName);

        // Now use the h:link to navigate back and check the conversation isn't propagated
        cloud = getFirstMatchingElement(blizzard, HtmlAnchor.class, "cloud-link").click();
        cloudName = getFirstMatchingElement(cloud, HtmlSpan.class, "cloudName").getTextContent();
        assertEquals(Cloud.NAME, cloudName);
    }

    @Test
    public void testConversationPropagationToNonExistentConversationLeadsException() throws Exception {
        WebClient client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(true);
        Page page = client.getPage(getPath("/cloud.xhtml", "nonExistentConversation"));
        if (page instanceof TextPage) {
            TextPage textPage = (TextPage) page;
            assertTrue(textPage.getContent().contains("NonexistentConversationException thrown properly"));
            assertTrue(textPage.getContent().contains("Conversation.isTransient: true"));
        } else {
            fail("Unexpected response type: " + page.getClass().getName());
        }
    }

    @Test
    public void testRedirectToConversation() throws Exception {
        WebClient client = new WebClient();
        HtmlPage page = client.getPage(getPath("/cloud.xhtml"));
        HtmlPage snowstorm = getFirstMatchingElement(page, HtmlSubmitInput.class, "snow").click();
        String name = getFirstMatchingElement(snowstorm, HtmlSpan.class, "snowstormName").getTextContent();
        assertEquals(Snowstorm.NAME, name);
        snowstorm = getFirstMatchingElement(snowstorm, HtmlSubmitInput.class, "go").click();
        name = getFirstMatchingElement(snowstorm, HtmlSpan.class, "snowstormName").getTextContent();
        assertEquals(Snowstorm.NAME, name);
    }

    // WELD-755
    @Test
    public void testEndAndBeginInSameRequestsKeepsSameCid() throws Exception {
        WebClient client = new WebClient();
        HtmlPage page = client.getPage(getPath("/tornado.xhtml"));
        String name = getFirstMatchingElement(page, HtmlSpan.class, "tornadoName").getTextContent();
        assertEquals("Pete", name);
        page = getFirstMatchingElement(page, HtmlSubmitInput.class, "beginConversation").click();
        name = getFirstMatchingElement(page, HtmlSpan.class, "tornadoName").getTextContent();
        assertEquals("Shane", name);
        page = getFirstMatchingElement(page, HtmlSubmitInput.class, "endAndBeginConversation").click();
        name = getFirstMatchingElement(page, HtmlSpan.class, "tornadoName").getTextContent();
        assertEquals("Shane", name);
    }

    @Test
    public void testLockingIssue() throws Exception {
        /*
         * click start
         * click redirect
         * click dummy
         * refresh browser or retry url.
         */
        WebClient client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = client.getPage(getPath("/locking-issue.xhtml"));
        assertEquals("Gavin", getFirstMatchingElement(page, HtmlSpan.class, "name").getTextContent());
        page = getFirstMatchingElement(page, HtmlSubmitInput.class, "start").click();
        assertEquals("Pete", getFirstMatchingElement(page, HtmlSpan.class, "name").getTextContent());
        String cid = getCid(page);
        getFirstMatchingElement(page, HtmlSubmitInput.class, "dummy").click();
        page = client.getPage(getPath("/locking-issue.xhtml?cid=" + cid));
        assertEquals("Pete", getFirstMatchingElement(page, HtmlSpan.class, "name").getTextContent());
    }

    @Test
    public void testExceptionInPreDestroy() throws Exception {
        WebClient client = new WebClient();

        // First, try a transient conversation

        // Access a page that throws an exception
        client.getPage(getPath("/thunderstorm.xhtml"));

        // Then access another page that doesn't and check the contexts are ok
        HtmlPage cloud = client.getPage(getPath("/cloud.xhtml"));
        String cloudName = getFirstMatchingElement(cloud, HtmlSpan.class, "cloudName").getTextContent();
        assertEquals(Cloud.NAME, cloudName);

        // Now start a conversation and access the page that throws an exception
        // again
        HtmlPage thunderstorm = getFirstMatchingElement(cloud, HtmlSubmitInput.class, "beginConversation").click();

        String thunderstormName = getFirstMatchingElement(thunderstorm, HtmlSpan.class, "thunderstormName").getTextContent();
        assertEquals(Thunderstorm.NAME, thunderstormName);
        cloud = getFirstMatchingElement(thunderstorm, HtmlSubmitInput.class, "cloud").click();

        // And navigate to another page, checking the conversation exists by
        // verifying that state is maintained
        cloudName = getFirstMatchingElement(cloud, HtmlSpan.class, "cloudName").getTextContent();
        assertEquals("bob", cloudName);
    }

    @Test
    public void testInvalidateCallsPreDestroy() throws Exception {
        WebClient client = new WebClient();

        // Now start a conversation
        HtmlPage cloud = client.getPage(getPath("/cloud.xhtml"));
        cloud = getFirstMatchingElement(cloud, HtmlSubmitInput.class, "hurricane").click();

        // Invalidate the session
        cloud = getFirstMatchingElement(cloud, HtmlSubmitInput.class, "invalidateSession").click();
        String cloudDestroyed = getFirstMatchingElement(cloud, HtmlSpan.class, "cloudDestroyed").getTextContent();
        assertEquals("true", cloudDestroyed);
    }

    @Test
    public void testInvalidateThenRedirect() throws Exception {
        WebClient client = new WebClient();

        // Now start a conversation
        HtmlPage cloud = client.getPage(getPath("/cloud.xhtml"));
        cloud = getFirstMatchingElement(cloud, HtmlSubmitInput.class, "hurricane").click();

        // Now invalidate the session and redirect
        cloud = getFirstMatchingElement(cloud, HtmlSubmitInput.class, "sleet").click();

        // Check that we are still working by verifying the page rendered
        String cloudName = getFirstMatchingElement(cloud, HtmlSpan.class, "cloudName").getTextContent();
        assertEquals(Cloud.NAME, cloudName);
    }

    @Test
    public void testExceptionInPostConstruct() throws Exception {
        WebClient client = new WebClient();

        // First, try a transient conversation

        client.getOptions().setThrowExceptionOnFailingStatusCode(false);

        // Access a page that throws an exception
        client.getPage(getPath("/hailstorm.xhtml"));

        // Then access another page that doesn't and check the contexts are ok
        HtmlPage cloud = client.getPage(getPath("/cloud.xhtml"));
        String cloudName = getFirstMatchingElement(cloud, HtmlSpan.class, "cloudName").getTextContent();
        assertEquals(Cloud.NAME, cloudName);

        // Now start a conversation and access the page that throws an exception
        // again
        Page hailstorm = getFirstMatchingElement(cloud, HtmlSubmitInput.class, "hail").click();

        String cid = getCid(hailstorm);

        cloud = client.getPage(getPath("/cloud.xhtml", cid));

        // And navigate to another page, checking the conversation exists by
        // verifying that state is maintained
        cloudName = getFirstMatchingElement(cloud, HtmlSpan.class, "cloudName").getTextContent();
        assertEquals("gavin", cloudName);
    }

    @Test
    public void testSuppressedConversationPropagation() throws Exception {
        WebClient client = new WebClient();

        // Access the start page
        HtmlPage cloud = client.getPage(getPath("/cloud.xhtml"));
        assertEquals(Cloud.NAME, getFirstMatchingElement(cloud, HtmlSpan.class, "cloudName").getTextContent());

        // Now start a conversation and check the cloud name changes
        HtmlPage page1 = getFirstMatchingElement(cloud, HtmlSubmitInput.class, Cloud.CUMULUS).click();
        assertEquals(Cloud.CUMULUS, getFirstMatchingElement(page1, HtmlSpan.class, "cloudName").getTextContent());
        String cid = getCid(page1);

        // Activate the conversation from a GET request
        HtmlPage page2 = client.getPage(getPath("/cloud.xhtml", cid));
        assertEquals(Cloud.CUMULUS, getFirstMatchingElement(page2, HtmlSpan.class, "cloudName").getTextContent());

        // Send a GET request with the "cid" parameter and suppressed conversation propagation (using conversationPropagation=none)
        HtmlPage page3 = client.getPage(getPath("/cloud.xhtml", cid) + "&conversationPropagation=none");
        assertEquals(Cloud.NAME, getFirstMatchingElement(page3, HtmlSpan.class, "cloudName").getTextContent());

        // Test again using the proprietary "nocid" parameter (kept for backwards compatibility)
        HtmlPage page4 = client.getPage(getPath("/cloud.xhtml", cid) + "&nocid=true");
        assertEquals(Cloud.NAME, getFirstMatchingElement(page4, HtmlSpan.class, "cloudName").getTextContent());
    }

    protected String getPath(String viewId, String cid) {
        StringBuilder builder = new StringBuilder(url.toString());
        builder.append(viewId);
        if (cid != null) {
            builder.append("?");
            builder.append(CID_REQUEST_PARAMETER_NAME);
            builder.append("=");
            builder.append(cid);
        }
        return builder.toString();
    }

    protected String getPath(String viewId) {
        return getPath(viewId, null);
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

    protected String getCid(Page page) {
        String url = page.getUrl().toString();
        if (url.indexOf("cid=") != url.lastIndexOf("cid=")) {
            throw new IllegalArgumentException("Invalid URL " + url);
        }
        return url.substring(url.indexOf("cid=") + 4);
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

}
