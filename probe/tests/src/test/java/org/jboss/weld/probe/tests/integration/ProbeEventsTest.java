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

import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.EVENT_INFO;
import static org.jboss.weld.probe.Strings.QUALIFIERS;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getPageAsJSONObject;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.probe.tests.integration.deployment.InvokingServlet;
import org.jboss.weld.probe.tests.integration.deployment.annotations.Collector;
import org.jboss.weld.probe.tests.integration.deployment.beans.ModelBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.SessionScopedBean;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ProbeEventsTest extends ProbeIntegrationTest {

    @ArquillianResource
    private URL url;

    private static final String TEST_ARCHIVE_NAME = "probe-events-test";

    @Deployment(testable = false)
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, TEST_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeEventsTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ProbeEventsTest.class.getPackage(), "beans.xml", "beans.xml")
                .addClass(InvokingServlet.class)
                .addPackage(ModelBean.class.getPackage())
                .addPackage(Collector.class.getPackage());
    }

    @Test
    public void testEventsEndpoint() throws IOException {
        WebClient client = invokeSimpleAction(url);
        JsonObject events = getPageAsJSONObject(JSONTestUtil.EVENTS_PATH + "?filters=kind:\"APPLICATION\"", url, client);

        ReadContext ctx = JsonPath.parse(events.toString());
        List<String> eventInfos = ctx.read("$." + DATA + "[*]." + EVENT_INFO, List.class);
        List<String> qualifiers = ctx.read("$." + DATA + "[*]." + QUALIFIERS + "[*]", List.class);
        assertTrue("No events found !", eventInfos.size() > 0);

        //check events
        assertTrue(eventInfos.contains(SessionScopedBean.MESSAGE_A));
        assertTrue(eventInfos.contains(SessionScopedBean.MESSAGE_B));
        assertTrue(eventInfos.contains(SessionScopedBean.MESSAGE_AB));

        //check event qualifiers
        assertTrue(qualifiers.contains("@" + Collector.class.getName().concat("(value=\"B\")")));
        assertTrue(qualifiers.contains("@" + Collector.class.getName().concat("(value=\"A\")")));
    }

    @Test
    public void testContainerEventsEndpoint() throws IOException {
        WebClient client = invokeSimpleAction(url);
        JsonObject events = getPageAsJSONObject(JSONTestUtil.EVENTS_PATH + "?filters=kind:\"CONTAINER\"", url, client);

        ReadContext ctx = JsonPath.parse(events.toString());
        List<String> qualifiers = ctx.read("$." + DATA + "[*]." + QUALIFIERS + "[*]", List.class);
        assertTrue("No events found !", qualifiers.size() > 0);

        //check event qualifiers
        assertTrue(qualifiers.contains("@javax.enterprise.context.Initialized(value=javax.enterprise.context.RequestScoped.class)"));
        assertTrue(qualifiers.contains("@javax.enterprise.context.Destroyed(value=javax.enterprise.context.RequestScoped.class)"));
        assertTrue(qualifiers.contains("@javax.enterprise.context.BeforeDestroyed(value=javax.enterprise.context.RequestScoped.class)"));
    }

}
