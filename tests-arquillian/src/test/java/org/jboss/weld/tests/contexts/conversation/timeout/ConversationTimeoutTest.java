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

package org.jboss.weld.tests.contexts.conversation.timeout;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
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

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * @author Marko Luksa
 *
 *         Timeout tests that address WELD-1452
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class ConversationTimeoutTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive getDeployment() {
        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(ConversationTimeoutTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(ConversationTimeoutTest.class.getPackage())
                .addAsWebInfResource(ConversationTimeoutTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @RunAsClient
    public void testConversationTimesout() throws Exception {
        WebClient client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);

        TextPage page = client.getPage(url + "/servlet/beginConversation");
        String cid = page.getContent();

        Thread.sleep(1000); // wait for conversation to time out

        page = client.getPage(url + "/servlet/testConversation?cid=" + cid);
        assertEquals(TimeoutFilter.NON_EXISTENT_CONVERSATION, page.getContent());
    }

    @Test
    @RunAsClient
    public void testConversationDoesNotTimeoutOnRedirect() throws Exception {
        WebClient client = new WebClient();

        TextPage page = client.getPage(url + "/servlet/beginConversation");
        String cid = page.getContent();

        // Conversation will expire in middle of request but should not timeout
        page = client.getPage(url + "/servlet/makeLongRequest?cid=" + cid);
        assertEquals(cid, page.getContent());

        // Simulate redirect
        page = client.getPage(url + "/servlet/testConversation?cid=" + cid);
        assertEquals(cid, page.getContent());
    }
}
