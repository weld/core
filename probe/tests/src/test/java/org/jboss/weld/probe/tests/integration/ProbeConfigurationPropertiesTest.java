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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import jakarta.enterprise.inject.spi.Extension;
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
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.context.DependentContext;
import org.jboss.weld.probe.ProbeFilter;
import org.jboss.weld.probe.tests.integration.deployment.InvokingServlet;
import org.jboss.weld.probe.tests.integration.deployment.annotations.Collector;
import org.jboss.weld.probe.tests.integration.deployment.beans.DummyBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.ModelBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.SessionScopedBean;
import org.jboss.weld.probe.tests.integration.deployment.extensions.TestExtension;
import org.jboss.weld.probe.tests.integration.deployment.interceptors.TestInterceptor;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ProbeConfigurationPropertiesTest extends ProbeIntegrationTest {

    @ArquillianResource
    private URL url;

    private static final String TEST_ARCHIVE_NAME = "probe-config-properties-test";

    @Deployment(testable = false)
    public static WebArchive deployFirst() {
        return ShrinkWrap.create(WebArchive.class, TEST_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeBeansTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(new BeansXml(BeanDiscoveryMode.ALL), "beans.xml")
                .addClass(InvokingServlet.class)
                .addPackage(TestInterceptor.class.getPackage())
                .addPackage(TestExtension.class.getPackage())
                .addPackage(ModelBean.class.getPackage())
                .addPackage(Collector.class.getPackage())
                .addAsServiceProvider(Extension.class, TestExtension.class)
                .addAsResource(PropertiesBuilder.newBuilder()
                        .set(ConfigurationKey.PROBE_INVOCATION_MONITOR_SKIP_JAVABEAN_PROPERTIES.get(), "false")
                        .set(ConfigurationKey.PROBE_INVOCATION_MONITOR_EXCLUDE_TYPE.get(),
                                "org.jboss.weld.servlet.WeldInitialListener|org.jboss.weld.servlet.WeldTerminalListener|" + InvokingServlet.class.getName()
                                        + "|"
                                        + ProbeFilter.class.getName())
                        .set(ConfigurationKey.PROBE_EVENT_MONITOR_CONTAINER_LIFECYCLE_EVENTS.get(), "true")
                        .set(ConfigurationKey.PROBE_EMBED_INFO_SNIPPET.get(), "false")
                        .set(ConfigurationKey.PROBE_EVENT_MONITOR_EXCLUDE_TYPE.get(), DummyBean.class.getName())
                        .build(), "weld.properties");
    }

    @Test
    public void testProbeFilterIsExcluded() throws IOException {
        WebClient webClient = invokeSimpleAction(url);
        JsonObject invocations = getPageAsJSONObject(INVOCATIONS_PATH, url, webClient);
        JsonArray invocationData = invocations.getJsonArray(DATA);
        int id = invocationData.getJsonObject(0).getInt(ID);
        JsonObject invocationTree = getPageAsJSONObject(INVOCATIONS_PATH + "/" + id, url, webClient);
        ReadContext ctx = JsonPath.parse(invocationTree.toString());
        String methodName = ctx.read("$." + METHOD_NAME, String.class);

        // servlet methods are omitted due InvokingServlet exlusion
        assertEquals(methodName, ModelBean.SIMPLE_CALL_METHOD_NAME);
    }

    @Test
    public void testAccesorMethodIsAvailableInInvocationTree() throws IOException {
        JsonArray childsOfInvocation = getChildInvocations();
        assertTrue("Cannot find any child invocations!", childsOfInvocation.size() > 0);

        ReadContext ctx = JsonPath.parse(childsOfInvocation.toString());
        List<String> methodNames = ctx.read("$.[*]." + METHOD_NAME, List.class);
        assertTrue("Cannot find accesor method " + SessionScopedBean.GETTER_METHOD_NAME, methodNames.contains(SessionScopedBean.GETTER_METHOD_NAME));
    }

    @Test
    public void testLifecycleEventsAreMonitored() throws IOException {
        WebClient client = invokeSimpleAction(url);
        JsonObject events = getPageAsJSONObject(JSONTestUtil.EVENTS_PATH + "?pageSize=0", url, client);

        ReadContext ctx = JsonPath.parse(events.toString());
        List<String> eventTypes = ctx.read("$." + DATA + "[*]." + TYPE, List.class);
        assertTrue("No event types found !", eventTypes.size() > 0);
        assertTrue("Cannot find PB event for " + DependentContext.class.getName(), eventTypes.contains("ProcessBean<DependentContext>"));
        assertTrue("Cannot find PIT event for " + InvokingServlet.class.getName(), eventTypes.contains("ProcessInjectionTarget<InvokingServlet>"));
    }

    @Test
    public void testEmbededInfoSnippetNOTAvailable() throws IOException {
        WebClient client = new WebClient();
        HtmlPage page = client.getPage(url.toString() + "test");
        System.out.println(page.getBody().asXml());
        assertFalse(page.getBody().asXml().toString().contains("Probe Development Tool"));
        assertFalse(page.getBody().asXml().toString().contains(TEST_ARCHIVE_NAME));
        assertFalse(page.getBody().asXml().toString().contains("The following snippet was automatically added by Weld"));
    }

    @Test
    public void testDummyBeanEventIsExcluded() throws IOException {
        WebClient client = invokeSimpleAction(url);
        JsonObject events = getPageAsJSONObject(JSONTestUtil.EVENTS_PATH + "?filters=kind:\"APPLICATION\"&pageSize=0", url, client);
        ReadContext ctx = JsonPath.parse(events.toString());
        List<String> eventsData = ctx.read("$." + DATA + "[*]." + TYPE, List.class);
        assertFalse("Found excluded event type " + DummyBean.class.getSimpleName() + "!", eventsData.contains(DummyBean.class.getSimpleName()));
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
