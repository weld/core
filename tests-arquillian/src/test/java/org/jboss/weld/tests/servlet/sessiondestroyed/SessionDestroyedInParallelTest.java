/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.servlet.sessiondestroyed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.Cookie;

/**
 *
 * @author Martin Kouba
 */
@Ignore("WFLY-1533")
@RunAsClient
@Category(Integration.class)
@RunWith(Arquillian.class)
public class SessionDestroyedInParallelTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class).addClasses(State.class, IntrospectServlet.class, SessionFoo.class);
    }

    @ArquillianResource
    URL contextPath;

    @Test
    public void testHttpSessionDestroyedInParallelRequest() throws Exception {

        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(true);

        // Init a new session
        TextPage initPage = client.getPage(contextPath + "introspect?mode=" + IntrospectServlet.MODE_INIT);
        String jsessionid = initPage.getContent().trim();
        assertNotNull(jsessionid);
        assertFalse(jsessionid.isEmpty());

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // Execute two requests in parallel - the second one invalidates the session
        WebRequest longTask = new WebRequest(IntrospectServlet.MODE_LONG_TASK, contextPath, jsessionid);
        WebRequest destroySession = new WebRequest(IntrospectServlet.MODE_DESTROY_SESSION, contextPath, jsessionid);

        try {
            Future<String> longTaskFuture = executorService.submit(longTask);
            Thread.sleep(500l);
            Future<String> destroySessionFuture = executorService.submit(destroySession);

            while (!longTaskFuture.isDone() || !destroySessionFuture.isDone()) {
                Thread.sleep(100l);
            }
            assertEquals("OK", longTaskFuture.get());
            assertEquals("OK", destroySessionFuture.get());
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * Note - htmlunit WebClient instance is not thread-safe.
     */
    private class WebRequest implements Callable<String> {

        private String mode;

        private URL contextPath;

        private String jsessionid;

        public WebRequest(String mode, URL contextPath, String jsessionid) {
            super();
            this.mode = mode;
            this.contextPath = contextPath;
            this.jsessionid = jsessionid;
        }

        @Override
        public String call() throws Exception {

            WebClient client = new WebClient();
            client.setThrowExceptionOnFailingStatusCode(false);
            client.getCookieManager().addCookie(new Cookie(contextPath.getHost(), "JSESSIONID", jsessionid));

            Page page = client.getPage(contextPath + "introspect?mode=" + mode);

            if (!(page instanceof TextPage)) {
                return "" + page.getWebResponse().getStatusCode();
            }
            TextPage textPage = (TextPage) page;
            System.out.println(textPage.getContent());
            return textPage.getContent();
        }

    }

}
