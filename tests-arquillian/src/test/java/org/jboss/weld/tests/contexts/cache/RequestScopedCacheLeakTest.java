/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.cache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class RequestScopedCacheLeakTest {

    @ArquillianResource
    private URL contextPath;

    @Deployment(testable = false)
    public static WebArchive getDeployment() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(RequestScopedCacheLeakTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(SimpleServlet.class, ConversationScopedBean.class);
    }

    @Test
    public void test() throws Exception {
        WebClient webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        for (int i = 0; i < 100; i++) {
            // first, send out a hundred of poisoning requests
            // each of these should leave a thread in a broken state
            sendRequest(webClient, i, true);
        }
        for (int i = 0; i < 100; i++) {
            // now send out normal requests to see if they are affected by the thread's broken state
            String result = sendRequest(webClient, i, false);
            Assert.assertFalse("Invalid state detected after " + (i + 1) + " requests", result.startsWith("bar"));
        }
    }

    private String sendRequest(WebClient webClient, int sequence, boolean poison)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        final String path = getPath("getAndSet", sequence, poison);
        return webClient.getPage(path).getWebResponse().getContentAsString().trim();
    }

    private String getPath(String test, int sequence, boolean poison) {
        String path = contextPath + "/servlet?action=" + test + "&sequence=" + sequence;
        if (poison) {
            path += "&poison=true";
        }
        return path;
    }
}
