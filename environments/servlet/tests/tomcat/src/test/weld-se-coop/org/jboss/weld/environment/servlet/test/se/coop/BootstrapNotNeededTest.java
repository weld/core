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

import java.io.File;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.inject.spi.CDI;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

public class BootstrapNotNeededTest {

    @Test
    public void testBootstrapNotNeeded() throws Exception {

        String id = UUID.randomUUID().toString();

        // First boostrap Weld SE
        try (WeldContainer container = new Weld(id).initialize()) {

            TestBean testBean = container.select(TestBean.class).get();
            assertNotNull(testBean);

            // @Initialized(ApplicationScoped.class) ContainerInitialized
            List<Object> initEvents = testBean.getInitEvents();
            assertEquals(1, initEvents.size());
            Object event = initEvents.get(0);
            assertTrue(event instanceof ContainerInitialized);
            assertEquals(id, ((ContainerInitialized)event).getContainerId());

            // Test CDIProvider
            CDI<Object> cdi = CDI.current();
            assertTrue(cdi instanceof WeldContainer);

            // servlet setup
            Tomcat tomcat = new Tomcat();
            tomcat.setBaseDir(new File(".").getAbsolutePath() + "/target");
            tomcat.setHostname("localhost");
            tomcat.setPort(8080);
            // we need to call get connector to trigger creation of default one
            tomcat.getConnector();

            String contextPath = "";
            String docBase = new File(".").getAbsolutePath() + "/target";
            Context context = tomcat.addContext(contextPath, docBase);

            String servletName = TestServlet.class.getSimpleName();
            String urlPattern = "/test";
            tomcat.addServlet(contextPath, servletName, TestServlet.class.getName());
            context.addServletMappingDecoded(urlPattern, servletName);

            // add Weld information
            context.addApplicationListener(Listener.class.getName());
            context.getServletContext().setAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME, container.getBeanManager());

            try {
                tomcat.start();

                // @Initialized(ApplicationScoped.class) ServletContext not fired
                assertEquals(1, initEvents.size());

                WebClient webClient = new WebClient();
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
                Page page = webClient.getPage("http://localhost:8080/test");
                assertEquals(testBean.getId(), page.getWebResponse().getContentAsString().trim());
            } finally {
                tomcat.stop();
            }
        }
    }

}
