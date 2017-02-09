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
package org.jboss.weld.environment.servlet.test.se.coop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;

import javax.enterprise.inject.spi.CDI;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

public class BootstrapNotNeededTest {

    @Test
    public void testBootstrapNotNeeded() throws Exception {

        // First boostrap Weld SE
        try (WeldContainer container = new Weld().initialize()) {

            TestBean testBean = container.instance().select(TestBean.class).get();
            assertNotNull(testBean);

            // Test CDIProvider
            CDI<Object> cdi = CDI.current();
            assertTrue(cdi instanceof WeldContainer);

            // Then start Jetty
            Server server = new Server(InetSocketAddress.createUnresolved("localhost", 8080));
            try {
                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/");
                server.setHandler(context);
                context.addServlet(TestServlet.class, "/test");
                context.setAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME, container.getBeanManager());
                context.addEventListener(new Listener());
                server.start();

                WebClient webClient = new WebClient();
                webClient.setThrowExceptionOnFailingStatusCode(true);
                Page page = webClient.getPage("http://localhost:8080/test");
                assertEquals(testBean.getId(), page.getWebResponse().getContentAsString().trim());
            } finally {
                server.stop();
            }
        }
    }

}
