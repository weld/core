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

import static junit.framework.Assert.assertTrue;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.EVENT_INFO;
import static org.jboss.weld.probe.Strings.QUALIFIERS;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getPageAsJSONObject;

import java.io.IOException;
import java.net.URL;

import javax.json.JsonArray;
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
        JsonObject events = getPageAsJSONObject(JSONTestUtil.EVENTS_PATH + "?filters=kind:APPLICATION", url, client);
        JsonArray eventsData = events.getJsonArray(DATA);
        assertTrue("No events found !", eventsData.size() > 0);

        //check events
        assertTrue(checkStringInArrayRecursively(SessionScopedBean.MESSAGE_A, EVENT_INFO, eventsData, false));
        assertTrue(checkStringInArrayRecursively(SessionScopedBean.MESSAGE_B, EVENT_INFO, eventsData, false));

        //check event qualifiers
        assertTrue(checkStringInArrayRecursively(Collector.class.getName().concat("(value=\\\"B\\\")"), QUALIFIERS, eventsData, false));
        assertTrue(checkStringInArrayRecursively(Collector.class.getName().concat("(value=\\\"A\\\")"), QUALIFIERS, eventsData, false));
    }

}
