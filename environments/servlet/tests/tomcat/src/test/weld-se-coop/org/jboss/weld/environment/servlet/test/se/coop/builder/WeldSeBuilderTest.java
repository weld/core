/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.servlet.test.se.coop.builder;

//import static org.junit.Assert.assertEquals;
//
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.servlet.ServletContextHandler;
//import org.jboss.weld.environment.se.Weld;
//import org.jboss.weld.environment.se.WeldContainer;
//import org.jboss.weld.environment.servlet.Listener;
//import org.junit.Test;
//
//import com.gargoylesoftware.htmlunit.Page;
//import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Testcase for WELD-1927
 *
 * @author Jozef Hartinger
 *
 */
public class WeldSeBuilderTest {

//    @Test
//    public void testPassingWeldSeBuilderToWeldServlet() throws Exception {
//        Weld builder = new Weld().disableDiscovery().beanClasses(Cat.class);
//        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.addEventListener(Listener.using(builder));
//        test(context);
//    }
//
//    @Test
//    public void testPassingWeldSeBuilderToWeldServletViaParam() throws Exception {
//        Weld builder = new Weld().disableDiscovery().beanClasses(Cat.class);
//        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.addEventListener(new Listener());
//        context.setAttribute(Listener.CONTAINER_ATTRIBUTE_NAME, builder);
//        test(context);
//    }
//
//    @Test
//    public void testPassingWeldSeContainerToWeldServlet() throws Exception {
//        try (WeldContainer weld = new Weld().disableDiscovery().beanClasses(Cat.class).initialize()) {
//            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//            context.addEventListener(Listener.using(weld));
//            test(context);
//        }
//    }
//
//    @Test
//    public void testPassingWeldSeContainerToWeldServletViaParam() throws Exception {
//        try (WeldContainer weld = new Weld().disableDiscovery().beanClasses(Cat.class).initialize()) {
//            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//            context.addEventListener(new Listener());
//            context.setAttribute(Listener.CONTAINER_ATTRIBUTE_NAME, weld);
//            test(context);
//        }
//    }
//
//    private void test(ServletContextHandler context) throws Exception {
//        Server server = new Server(8080);
//        context.setContextPath("/");
//        server.setHandler(context);
//        context.addServlet(TestServlet.class, "/test");
//        server.start();
//
//        try {
//            WebClient webClient = new WebClient();
//            webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
//            Page page = webClient.getPage("http://localhost:8080/test");
//            assertEquals("Kitty", page.getWebResponse().getContentAsString().trim());
//        } finally {
//            // no need to stop Weld here, it is stopped by weld-servlet
//            server.stop();
//        }
//    }
}
