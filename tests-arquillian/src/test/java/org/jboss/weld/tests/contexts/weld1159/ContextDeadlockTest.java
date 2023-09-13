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
package org.jboss.weld.tests.contexts.weld1159;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
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

import com.gargoylesoftware.htmlunit.WebClient;

/**
 *
 * Reproduces deadlock between creation locks in the application and session context.
 *
 * Two pairs of beans are constructed in parallel:
 *
 * SessionScopedFoo -> ApplicationScopedFoo
 * ApplicationScopedBar -> SessionScopedBar
 *
 * We use a {@link CountDownLatch} to simulate that both threads get into state when the first bean of the pair
 * (SessionScopedFoo and ApplicationScopedFoo) is created and before the dependency is created. This causes deadlock
 * because the first thread
 *
 * - owns the session lock
 * - waits for application lock
 *
 * whereas the second thread
 *
 * - owns the application lock
 * - waits for the session lock
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class ContextDeadlockTest {

    @ArquillianResource
    private volatile URL url;

    private final WebClient client = new WebClient();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Deployment(testable = false)
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(ContextDeadlockTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(AbstractBean.class, ApplicationScopedFoo.class, ApplicationScopedBar.class, SessionScopedFoo.class,
                        SessionScopedBar.class,
                        TestServlet.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void test() throws Exception {
        client.getOptions().setTimeout(15000);

        List<Callable<Void>> requests = new LinkedList<Callable<Void>>();
        requests.add(new ConcurrentRequest(url.toString() + "/foo"));
        requests.add(new ConcurrentRequest(url.toString() + "/bar"));

        for (Future<Void> result : executor.invokeAll(requests)) {
            result.get();
        }
    }

    @After
    public void shutdown() {
        executor.shutdownNow();
    }

    private class ConcurrentRequest implements Callable<Void> {

        private final String url;

        public ConcurrentRequest(String url) {
            this.url = url;
        }

        public Void call() throws Exception {
            try {
                client.getPage(url);
            } catch (SocketTimeoutException e) {
                throw new RuntimeException("The request for " + url + " timed out. It is very likely that a deadlock occured.");
            }
            return null;
        }
    }
}
