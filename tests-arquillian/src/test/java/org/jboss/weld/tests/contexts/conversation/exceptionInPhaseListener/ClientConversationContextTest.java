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
package org.jboss.weld.tests.contexts.conversation.exceptionInPhaseListener;

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
import static org.junit.Assert.assertEquals;

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
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * @author Nicklas Karlsson
 * @author Dan Allen
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class ClientConversationContextTest {

    public static final String CID_REQUEST_PARAMETER_NAME = "cid";

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(ClientConversationContextTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(ConversationTestPhaseListener.class, Cloud.class)
                .addAsWebInfResource(ClientConversationContextTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ClientConversationContextTest.class.getPackage(), "faces-config.xml", "faces-config.xml")
                .addAsWebResource(ClientConversationContextTest.class.getPackage(), "cloud.xhtml", "cloud.xhtml")
                .addAsWebResource(ClientConversationContextTest.class.getPackage(), "thunderstorm.xhtml", "thunderstorm.xhtml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testExceptionPhaseListener() throws Exception {
        WebClient client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);

        // First, try a transient conversation

        // Access a page that throws an exception
        client.getPage(getPath("/thunderstorm.xhtml"));

        // Then access another page that doesn't and check the contexts are ok
        HtmlPage cloud = client.getPage(getPath("/cloud.xhtml"));
        String cloudName = getFirstMatchingElement(cloud, HtmlSpan.class, "cloudName").getTextContent();
        assertEquals(Cloud.NAME, cloudName);

        // Now start a conversation
        HtmlPage thunderstorm = getFirstMatchingElement(cloud, HtmlSubmitInput.class, "beginConversation").click();
        String cid = getCid(thunderstorm);

        //  and access the page that throws an exception again
        getFirstMatchingElement(cloud, HtmlSubmitInput.class, "thunderstorm").click();

        cloud = client.getPage(getPath("/cloud.xhtml", cid));

        // And navigate to another page, checking the conversation exists by verifying that state is maintained
        cloudName = getFirstMatchingElement(cloud, HtmlSpan.class, "cloudName").getTextContent();
        assertEquals("gavin", cloudName);
    }

    protected String getPath(String viewId, String cid) {
        return getPath(viewId) + "?" + CID_REQUEST_PARAMETER_NAME + "=" + cid;
    }

    protected String getPath(String viewId) {
        return url + viewId;
    }

    protected String getCid(Page page) {
        String url = page.getUrl().toString();
        return url.substring(url.indexOf("cid=") + 4);
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
