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
package org.jboss.weld.probe.integration.tests;

import static junit.framework.Assert.assertTrue;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.METHOD_NAME;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.INVOCATIONS_PATH;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.getPageAsJSONObject;

import java.io.IOException;
import java.net.URL;

import javax.json.JsonArray;
import javax.json.JsonObject;

import com.gargoylesoftware.htmlunit.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.probe.integration.tests.annotations.Collector;
import org.jboss.weld.probe.integration.tests.beans.ModelBean;
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
                .addPackage(ProbeInvocationsTest.class.getPackage())
                .addPackage(ModelBean.class.getPackage())
                .addPackage(Collector.class.getPackage())
                // exclude InvokingServlet from monitoring
                .addAsResource("weld.properties", "weld.properties");
    }

    @Test
    public void testInvocationsEndpoint() throws IOException {
        WebClient client = invokeSimpleAction(url);
        JsonObject invocations = getPageAsJSONObject(INVOCATIONS_PATH, url, client);
        JsonArray invocationData = invocations.getJsonArray(DATA);
        assertTrue("No invocations in invocation tree!", invocationData.size() > 0);
        assertTrue(checkStringInArrayRecursively("simpleCall", METHOD_NAME, invocationData, false));
    }

}
