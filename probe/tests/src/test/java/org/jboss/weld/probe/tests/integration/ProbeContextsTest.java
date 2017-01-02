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
package org.jboss.weld.probe.tests.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.jboss.weld.probe.Strings.BEAN_CLASS;
import static org.jboss.weld.probe.Strings.INSTANCES;
import static org.jboss.weld.probe.Strings.SCOPE;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.APPLICATION_CONTEXTS_PATH;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.SESSION_CONTEXTS_PATH;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getPageAsJSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.json.JsonObject;

import com.gargoylesoftware.htmlunit.WebClient;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.probe.tests.integration.deployment.InvokingServlet;
import org.jboss.weld.probe.tests.integration.deployment.annotations.Collector;
import org.jboss.weld.probe.tests.integration.deployment.beans.ApplicationScopedObserver;
import org.jboss.weld.probe.tests.integration.deployment.beans.ModelBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.SessionScopedBean;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
public class ProbeContextsTest extends ProbeIntegrationTest {

    @ArquillianResource
    private URL url;

    private static final String TEST_ARCHIVE_NAME = "probe-contexts-test";

    @Deployment(testable = false)
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, TEST_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeContextsTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ProbeContextsTest.class.getPackage(), "beans.xml", "beans.xml")
                .addClass(InvokingServlet.class)
                .addPackage(ModelBean.class.getPackage())
                .addPackage(Collector.class.getPackage());
    }

    @Test
    public void testSessionContextsEndpoint() throws IOException {
        WebClient client = invokeSimpleAction(url);
        JsonObject sessionContext = getPageAsJSONObject(SESSION_CONTEXTS_PATH, url, client);
        ReadContext ctx = JsonPath.parse(sessionContext.toString());
        List<String> beanClasses = ctx.read("$." + INSTANCES + "[*]." + BEAN_CLASS, List.class);

        assertEquals(SessionScoped.class.getName(), sessionContext.getString(SCOPE));
        assertTrue("Cannot find initialized context for " + sessionContext.getString(SCOPE), beanClasses.contains(SessionScopedBean.class.getName()));
    }

    @Test
    public void testApplicationContextsEndpoint() throws IOException {
        WebClient client = invokeSimpleAction(url);
        JsonObject applicationContext = getPageAsJSONObject(APPLICATION_CONTEXTS_PATH, url, client);
        ReadContext ctx = JsonPath.parse(applicationContext.toString());
        List<String> beanClasses = ctx.read("$." + INSTANCES + "[*]." + BEAN_CLASS, List.class);

        assertEquals(ApplicationScoped.class.getName(), applicationContext.getString(SCOPE));
        assertTrue("Cannot find initialized context for " + applicationContext.getString(SCOPE),
                beanClasses.contains(ApplicationScopedObserver.class.getName()));
    }

}
