/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
        return ShrinkWrap.create(WebArchive.class).addClasses(Servlet.class, ObservingBean.class, SessionScopedBean.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
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
