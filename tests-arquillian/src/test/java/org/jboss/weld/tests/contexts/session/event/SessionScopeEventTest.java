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
package org.jboss.weld.tests.contexts.session.event;

import static org.junit.Assert.assertTrue;

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

@Category(Integration.class)
@RunWith(Arquillian.class)
public class SessionScopeEventTest {

    @ArquillianResource(Servlet.class)
    private URL url;

    @Deployment(testable = false)
    public static WebArchive getDeployment() {
        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(SessionScopeEventTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(Servlet.class, ObservingBean.class, SessionScopedBean.class).addAsWebInfResource(
                        EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void test() throws Exception {
        WebClient client = new WebClient();

        {
            TextPage page = client.getPage(url);
            assertTrue(page.getContent().contains("Initialized sessions:1")); // the current session
            assertTrue(page.getContent().contains("Destroyed sessions:0")); // not destroyed yet
        }

        {
            // nothing should change
            TextPage page = client.getPage(url);
            assertTrue(page.getContent().contains("Initialized sessions:1"));
            assertTrue(page.getContent().contains("Destroyed sessions:0"));
        }
        {
            // invalidate the session
            TextPage page = client.getPage(url + "/invalidate");
            assertTrue(page.getContent().contains("Initialized sessions:1"));
            // the context is destroyed after the response is sent
            // verify in the next request
            assertTrue(page.getContent().contains("Destroyed sessions:0"));
        }
        {
            TextPage page = client.getPage(url);
            // new session context was initialized
            assertTrue(page.getContent().contains("Initialized sessions:2"));
            // the previous one was destroyed
            assertTrue(page.getContent().contains("Destroyed sessions:1"));
        }
    }
}
