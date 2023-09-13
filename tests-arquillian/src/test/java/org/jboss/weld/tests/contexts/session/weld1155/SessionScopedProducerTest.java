/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.session.weld1155;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.Cookie;

/**
 * @see https://issues.jboss.org/browse/WELD-1155
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class SessionScopedProducerTest {

    @ArquillianResource
    private volatile URL url;

    private final WebClient client = new WebClient();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Deployment(testable = false)
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(SessionScopedProducerTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(Producer.class, Product.class, TestServlet.class, SessionScopedBean.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void test() throws Exception {
        client.getPage(url.toString() + "/initial");
        Set<Cookie> cookies = client.getCookieManager().getCookies();

        List<Callable<Void>> requests = new LinkedList<Callable<Void>>();
        requests.add(new ConcurrentRequest(cookies));
        requests.add(new ConcurrentRequest(cookies));

        for (Future<Void> result : executor.invokeAll(requests)) {
            result.get();
        }
    }

    @After
    public void shutdown() {
        executor.shutdownNow();
    }

    private class ConcurrentRequest implements Callable<Void> {

        private final WebClient client;

        public ConcurrentRequest(Set<Cookie> cookies) {
            client = new WebClient();
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
            for (Cookie cookie : cookies) {
                client.getCookieManager().addCookie(cookie);
            }
        }

        public Void call() throws Exception {
            Page page = client.getPage(url);
            if (page.getWebResponse().getStatusCode() == 500) {
                throw new RuntimeException(page.getWebResponse().getContentAsString());
            }
            return null;
        }
    }
}
