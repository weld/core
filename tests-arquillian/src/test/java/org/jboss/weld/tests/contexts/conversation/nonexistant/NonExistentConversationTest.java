/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.contexts.conversation.nonexistant;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
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

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Tests that in case of non-existent conversation ID, there is an {@code @Initialized} event fired for the newly
 * associated conversation
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class NonExistentConversationTest {

    @ArquillianResource
    URL url;

    @Deployment(testable = false)
    public static WebArchive getDeployment() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(NonExistentConversationTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(NonExistentConversationTest.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

    }

    @Test
    public void testInitEventFired() throws IOException {

        WebClient client = new WebClient();

        // assert initial state
        TextPage initPage = client.getPage(url + "init");
        TextPage destroyedPage = client.getPage(url + "destroyed");
        assertTrue(initPage.getContent().contains("init: 0"));
        assertTrue(destroyedPage.getContent().contains("destroyed: 0"));

        //first try the usual way, this spawns new conversation
        client.getPage(url + "begin");
        initPage = client.getPage(url + "init");
        destroyedPage = client.getPage(url + "destroyed");
        assertTrue(initPage.getContent().contains("init: 1"));
        assertTrue(destroyedPage.getContent().contains("destroyed: 1"));

        // get non-existing conversation
        client.getPage(url + "begin?cid=9999");
        initPage = client.getPage(url + "init");
        destroyedPage = client.getPage(url + "destroyed");
        assertTrue(initPage.getContent().contains("init: 2"));
        assertTrue(destroyedPage.getContent().contains("destroyed: 2"));

        // get non-existing conversation wtih empty cid
        client.getPage(url + "begin?cid=");
        initPage = client.getPage(url + "init");
        destroyedPage = client.getPage(url + "destroyed");
        assertTrue(initPage.getContent().contains("init: 3"));
        assertTrue(destroyedPage.getContent().contains("destroyed: 3"));
    }
}
