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
import static org.jboss.weld.probe.Strings.CHILDREN;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.ID;
import static org.jboss.weld.probe.Strings.INTERCEPTED_BEAN;
import static org.jboss.weld.probe.Strings.METHOD_NAME;
import static org.jboss.weld.probe.Strings.TYPE;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.INVOCATIONS_PATH;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getPageAsJSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

import com.gargoylesoftware.htmlunit.WebClient;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.probe.ProbeFilter;
import org.jboss.weld.probe.tests.integration.deployment.InvokingServlet;
import org.jboss.weld.probe.tests.integration.deployment.annotations.Collector;
import org.jboss.weld.probe.tests.integration.deployment.beans.ApplicationScopedObserver;
import org.jboss.weld.probe.tests.integration.deployment.beans.ConversationBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.ModelBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.SessionScopedBean;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.jboss.weld.tests.util.SystemPropertiesLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
public class ProbeInvocationsTest extends ProbeIntegrationTest {

    @ArquillianResource
    private URL url;

    private static final String TEST_ARCHIVE_NAME = "probe-invocations-test";

    @Deployment(testable = false)
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, TEST_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeInvocationsTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ProbeInvocationsTest.class.getPackage(), "beans.xml", "beans.xml")
                .addClasses(InvokingServlet.class, SystemPropertiesLoader.class, PropertiesBuilder.class)
                .addPackage(ModelBean.class.getPackage())
                .addPackage(Collector.class.getPackage())
                .addAsResource(PropertiesBuilder.newBuilder().set(ConfigurationKey.PROBE_INVOCATION_MONITOR_EXCLUDE_TYPE.get(),
                        "org.jboss.weld.servlet.WeldInitialListener|org.jboss.weld.servlet.WeldTerminalListener|" + InvokingServlet.class.getName() + "|"
                                + ProbeFilter.class.getName())
                        .build(), "weld.properties");
    }

    @Test
    public void testInvocationsEndpoint() throws IOException {
        WebClient client = invokeSimpleAction(url);
        JsonObject invocations = getPageAsJSONObject(INVOCATIONS_PATH, url, client);
        ReadContext ctx = JsonPath.parse(invocations.toString());
        List<String> methodNames = ctx.read("$." + DATA + "[*]." + METHOD_NAME, List.class);

        assertTrue("No invocations in invocation tree!", methodNames.size() > 0);
        assertTrue(methodNames.contains(ModelBean.SIMPLE_CALL_METHOD_NAME));
    }

    @Test
    public void testInvocationTreeDetail() throws IOException {
        WebClient webClient = invokeSimpleAction(url);
        JsonObject invocations = getPageAsJSONObject(INVOCATIONS_PATH, url, webClient);
        JsonArray invocationData = invocations.getJsonArray(DATA);

        //there is at least one invocation tree
        int id = invocationData.getJsonObject(0).getInt(ID);
        JsonObject invocationTree = getPageAsJSONObject(INVOCATIONS_PATH + "/" + id, url, webClient);
        assertEquals(ModelBean.class.getName(), invocationTree.getJsonObject(INTERCEPTED_BEAN).getString(BEAN_CLASS));
        JsonArray children = invocationTree.getJsonArray(CHILDREN);
        assertTrue("Cannot find any child invocations!", children.size() > 0);

        JsonObject sessionScopedCreation = children.getJsonObject(0);
        assertEquals(SessionScopedBean.class.getName(), sessionScopedCreation.getJsonObject(INTERCEPTED_BEAN).getString(BEAN_CLASS));
        assertEquals("public org.jboss.weld.probe.tests.integration.deployment.beans.SessionScopedBean()", sessionScopedCreation.getString(METHOD_NAME));

        //test sessionScopedBean child invocation
        JsonObject sessionScopedInvocation = children.getJsonObject(1);
        assertEquals(SessionScopedBean.class.getName(), sessionScopedInvocation.getJsonObject(INTERCEPTED_BEAN).getString(BEAN_CLASS));
        assertEquals(SessionScopedBean.SOME_METHOD_NAME, sessionScopedInvocation.getString(METHOD_NAME));

        ReadContext ctx = JsonPath.parse(sessionScopedInvocation.toString());
        List<String> beanClasses = ctx.read("$." + CHILDREN + "[*]." + INTERCEPTED_BEAN + "." + BEAN_CLASS);
        List<String> methodNames = ctx.read("$." + CHILDREN + "[*]." + METHOD_NAME);

        assertTrue(beanClasses.contains(ApplicationScopedObserver.class.getName()));
        assertTrue(methodNames.contains("listen"));
        assertTrue(methodNames.contains("listen1"));
        assertTrue(methodNames.contains("listen2"));

        JsonObject conversationScopedCreation = children.getJsonObject(2);
        assertEquals(ConversationBean.class.getName(), conversationScopedCreation.getJsonObject(INTERCEPTED_BEAN).getString(BEAN_CLASS));
        assertEquals("public org.jboss.weld.probe.tests.integration.deployment.beans.ConversationBean()", conversationScopedCreation.getString(METHOD_NAME));

        //test conversationScopedBean child invocation
        JsonObject conversationScopedInvocation = children.getJsonObject(3);
        assertEquals(ConversationBean.class.getName(), conversationScopedInvocation.getJsonObject(INTERCEPTED_BEAN).getString(BEAN_CLASS));
        assertEquals("start", conversationScopedInvocation.getString(METHOD_NAME));
        assertEquals("BUSINESS", conversationScopedInvocation.getString(TYPE));
    }

}
