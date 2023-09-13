/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.conversation.timeout.concurrent;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.test.util.Timer;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.Cookie;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class ConversationLockTimeoutTest {

    private static final String JSESSIONID = "JSESSIONID";

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive getDeployment() {
        WebArchive testDeployment = ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(ConversationLockTimeoutTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(ConversationLockTimeoutTest.class.getPackage())
                .addClass(Timer.class)
                .addAsWebInfResource(ConversationLockTimeoutTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(
                        PropertiesBuilder.newBuilder()
                                .set(ConfigurationKey.CONVERSATION_CONCURRENT_ACCESS_TIMEOUT.get(), "3000").build(),
                        "weld.properties");
        return testDeployment;
    }

    @Test
    public void testLongerConversationLockTimeout() throws Exception {
        WebClient client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);

        TextPage initPage = client.getPage(url + "inspect?mode=" + InspectServlet.MODE_INIT);
        String cid = extractCid(initPage.getContent());
        Assert.assertNotNull(cid);
        Assert.assertFalse(cid.isEmpty());
        String jsessionid = client.getCookieManager().getCookie(JSESSIONID).getValue();
        Assert.assertNotNull(jsessionid);
        Assert.assertFalse(jsessionid.isEmpty());

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        WebRequest longTask = new WebRequest(InspectServlet.MODE_LONG_TASK, url, cid, jsessionid);
        WebRequest busyRequest = new WebRequest(InspectServlet.MODE_BUSY_REQUEST, url, cid, jsessionid);

        final Future<String> longTaskFuture = executorService.submit(longTask);
        Timer timer = Timer.startNew(1000l);
        final Future<String> busyRequestFuture = executorService.submit(busyRequest);
        timer.setSleepInterval(100l).setDelay(2, TimeUnit.SECONDS)
                .addStopCondition(() -> longTaskFuture.isDone() || busyRequestFuture.isDone()).start();

        Assert.assertEquals("OK", longTaskFuture.get());
        Assert.assertEquals("Conversation locked", busyRequestFuture.get());
        executorService.shutdown();
    }

    /**
     * Note - htmlunit WebClient instance is not thread-safe.
     */
    private class WebRequest implements Callable<String> {

        private String mode;

        private URL contextPath;

        private String cid;

        private String jsessionid;

        public WebRequest(String mode, URL contextPath, String cid, String jsessionid) {
            super();
            this.mode = mode;
            this.contextPath = contextPath;
            this.cid = cid;
            this.jsessionid = jsessionid;
        }

        @Override
        public String call() throws Exception {

            WebClient client = new WebClient();
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
            client.getCookieManager().addCookie(new Cookie(contextPath.getHost(), JSESSIONID, jsessionid));

            Page page = client.getPage(contextPath + "inspect?mode=" + mode + "&cid=" + cid);

            if (!(page instanceof TextPage)) {
                return "" + page.getWebResponse().getStatusCode();
            }
            TextPage textPage = (TextPage) page;
            return textPage.getContent();
        }

    }

    private String extractCid(String content) {
        String[] tokens = content.split("::");
        if (tokens.length != 2) {
            throw new IllegalArgumentException();
        }
        return tokens[0];
    }
}
