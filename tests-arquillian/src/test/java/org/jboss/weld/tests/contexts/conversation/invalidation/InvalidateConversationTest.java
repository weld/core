/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.conversation.invalidation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Tests that long-running conversation does not get destroyed until the end of servlet.service() method when invoking
 * session.invalidate().
 *
 * @see WELD-2052
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class InvalidateConversationTest {

    @ArquillianResource
    URL url;

    @Deployment(testable = false)
    public static WebArchive getDeployment() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(InvalidateConversationTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(InvalidateConversationTest.class.getPackage())
                .addClass(ActionSequence.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testConversationDestroyedAfterRequestEnds() throws IOException {

        WebClient client = new WebClient();

        //reset ActionSequence on server
        client.getPage(url + "resetSequence");

        // trigger conversation
        TextPage page = client.getPage(url + "begin");
        String cid = page.getContent().trim();
        // trigger session invalidation
        page = client.getPage(url + "invalidate?cid=" + cid);

        // invoke third request to get complete result of ActionSequence from server
        page = client.getPage(url + "result");
        String result = page.getContent();

        // prepare expected result
        ActionSequence.reset();
        ActionSequence.addAction("conversationCreated");
        ActionSequence.addAction("beforeInvalidate");
        ActionSequence.addAction("afterInvalidate");
        ActionSequence.addAction("conversationDestroyed");

        assertEquals(ActionSequence.getSequence().dataToCsv(), result);
    }

    @Test
    public void testAllLongRunningConversationsGetDestroyedAfterRequest() throws IOException {
        String firstCid;
        String secondCid;

        WebClient client = new WebClient();
        //reset ActionSequence on server
        client.getPage(url + "resetSequence");

        // initiate conversations
        TextPage page = client.getPage(url + "begin");
        firstCid = page.getContent().trim();

        page = client.getPage(url + "begin");
        secondCid = page.getContent().trim();
        assertFalse(firstCid.equals(secondCid));

        // trigger session invalidation with one cid
        page = client.getPage(url + "invalidate?cid=" + secondCid);

        // invoke third request to get complete result of ActionSequence from server
        page = client.getPage(url + "result");
        String result = page.getContent();

        // prepare expected result
        ActionSequence.reset();
        ActionSequence.addAction("conversationCreated");
        ActionSequence.addAction("conversationCreated");
        ActionSequence.addAction("beforeInvalidate");
        ActionSequence.addAction("afterInvalidate");
        ActionSequence.addAction("conversationDestroyed");
        ActionSequence.addAction("conversationDestroyed");

        assertEquals(ActionSequence.getSequence().dataToCsv(), result);
    }

}
