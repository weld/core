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

import static org.jboss.weld.probe.Strings.CHILDREN;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.ID;
import static org.jboss.weld.probe.Strings.METHOD_NAME;
import static org.jboss.weld.probe.Strings.TYPE;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.INVOCATIONS_PATH;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getPageAsJSONObject;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.enterprise.inject.spi.Extension;
import javax.json.JsonArray;
import javax.json.JsonObject;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.context.DependentContext;
import org.jboss.weld.probe.tests.integration.deployment.InvokingServlet;
import org.jboss.weld.probe.tests.integration.deployment.annotations.Collector;
import org.jboss.weld.probe.tests.integration.deployment.beans.ModelBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.SessionScopedBean;
import org.jboss.weld.probe.tests.integration.deployment.extensions.TestExtension;
import org.jboss.weld.probe.tests.integration.deployment.interceptors.TestInterceptor;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ProbeDefaultConfigurationPropertiesTest extends ProbeIntegrationTest {

    @ArquillianResource
    private URL url;

    private static final String TEST_ARCHIVE_NAME = "probe-default-config-properties-test";

    @Deployment(testable = false)
    public static WebArchive deploySecond() {
        return ShrinkWrap.create(WebArchive.class, TEST_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeBeansTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ProbeBeansTest.class.getPackage(), "beans.xml", "beans.xml")
                .addClass(InvokingServlet.class)
                .addPackage(TestInterceptor.class.getPackage())
                .addPackage(TestExtension.class.getPackage())
                .addPackage(ModelBean.class.getPackage())
                .addPackage(Collector.class.getPackage())
                .addAsServiceProvider(Extension.class, TestExtension.class);
    }

    @Test
    public void testAccesorMethodIsNOTAvailableInInvocationTree() throws IOException {
        JsonArray childsOfInvocation = getChildInvocations();
        assertTrue("Cannot find any child invocations!", childsOfInvocation.size() > 0);

        ReadContext ctx = JsonPath.parse(childsOfInvocation.toString());
        List<String> methodNames = ctx.read("$.[*]." + METHOD_NAME, List.class);
        assertTrue("Found accesor method " + SessionScopedBean.GETTER_METHOD_NAME, !methodNames.contains(SessionScopedBean.GETTER_METHOD_NAME));
    }

    @Test
    public void testEmbededInfoSnippetAvailable() throws IOException {
        WebClient client = new WebClient();
        HtmlPage page = client.getPage(url.toString() + "test");
        assertTrue(page.getBody().asXml().toString().contains("Probe Development Tool"));
        assertTrue(page.getBody().asXml().toString().contains(TEST_ARCHIVE_NAME));
        assertTrue(page.getBody().asXml().toString().contains("The following snippet was automatically added by Weld"));
    }

    @Test
    public void testLifecycleEventsAreNOTMonitored() throws IOException {
        WebClient client = invokeSimpleAction(url);
        JsonObject events = getPageAsJSONObject(JSONTestUtil.EVENTS_PATH + "?pageSize=0", url, client);

        ReadContext ctx = JsonPath.parse(events.toString());
        List<String> eventTypes = ctx.read("$." + DATA + "[*]." + TYPE, List.class);
        assertFalse("Found PB event for " + DependentContext.class.getName(), eventTypes.contains("ProcessBean<DependentContext>"));
        assertFalse("Found PIT event for " + InvokingServlet.class.getName(), eventTypes.contains("ProcessInjectionTarget<InvokingServlet>"));
    }

    private JsonArray getChildInvocations() throws IOException {
        WebClient webClient = invokeSimpleAction(url);
        JsonObject invocations = getPageAsJSONObject(INVOCATIONS_PATH, url, webClient);
        JsonArray invocationData = invocations.getJsonArray(DATA);

        int id = invocationData.getJsonObject(0).getInt(ID);
        JsonObject invocationTree = getPageAsJSONObject(INVOCATIONS_PATH + "/" + id, url, webClient);
        return invocationTree.getJsonArray(CHILDREN);
    }

}
