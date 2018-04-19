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
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.OBSERVED_TYPE;
import static org.jboss.weld.probe.Strings.QUALIFIERS;
import static org.jboss.weld.probe.Strings.RECEPTION;
import static org.jboss.weld.probe.Strings.TX_PHASE;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getAllJsonObjectsByClass;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getPageAsJSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.json.JsonArray;
import javax.json.JsonObject;

import com.gargoylesoftware.htmlunit.WebClient;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.probe.tests.integration.deployment.InvokingServlet;
import org.jboss.weld.probe.tests.integration.deployment.annotations.Collector;
import org.jboss.weld.probe.tests.integration.deployment.beans.ApplicationScopedObserver;
import org.jboss.weld.probe.tests.integration.deployment.beans.ModelBean;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ProbeObserversTest extends ProbeIntegrationTest {

    @ArquillianResource
    private URL url;

    private static final String TEST_ARCHIVE_NAME = "probe-observers-test";

    @Deployment(testable = false)
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, TEST_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeObserversTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ProbeObserversTest.class.getPackage(), "beans.xml", "beans.xml")
                .addClass(InvokingServlet.class)
                .addPackage(ModelBean.class.getPackage())
                .addPackage(Collector.class.getPackage());
    }

    @Test
    public void testObserversEndpoint() throws IOException {
        WebClient client = invokeSimpleAction(url);
        JsonObject observers = getPageAsJSONObject(JSONTestUtil.OBSERVERS_PATH_ALL, url, client);
        ReadContext ctx = JsonPath.parse(observers.toString());
        List<String> beanClasses = ctx.read("$." + DATA + "[*]." + BEAN_CLASS, List.class);
        List<String> txPhases = ctx.read("$." + DATA + "[*]." + TX_PHASE, List.class);
        List<String> qualifiers = ctx.read("$." + DATA + "[*]." + QUALIFIERS + "[*]", List.class);

        //check observers
        assertTrue(beanClasses.contains(ApplicationScopedObserver.class.getName()));
        assertTrue(qualifiers.contains("@" + Collector.class.getName().concat("(value=\"A\")")));
        assertTrue(qualifiers.contains("@" + Collector.class.getName().concat("(value=\"B\")")));
        assertTrue(txPhases.contains(TransactionPhase.BEFORE_COMPLETION.name()));

        JsonArray observersData = observers.getJsonArray(DATA);
        assertTrue("No observers found !", observersData.size() > 0);
        List<JsonObject> jsonObservers = getAllJsonObjectsByClass(ApplicationScopedObserver.class, observersData);

        // find observer only with Collector qualifier with value "B"
        Optional<JsonObject> observerWithQualifier = jsonObservers.stream()
                .filter((JsonObject o) -> o.getJsonArray(QUALIFIERS) != null && o.getJsonArray(QUALIFIERS).size() == 1 && o.getJsonArray(QUALIFIERS)
                        .getString(0).equals("@" + Collector.class.getName().concat("(value=\"B\")"))).findAny();

        assertEquals(Reception.ALWAYS.name(), observerWithQualifier.get().getString(RECEPTION));
        assertEquals(String.class.getName(), observerWithQualifier.get().getString(OBSERVED_TYPE));
        assertEquals(TransactionPhase.IN_PROGRESS.name(), observerWithQualifier.get().getString(TX_PHASE));

        // find next observer
        Optional<JsonObject> observerWithReception = jsonObservers.stream()
                .filter((JsonObject o) -> o.getString(RECEPTION).equals(Reception.IF_EXISTS.name())).findAny();

        assertEquals(Properties.class.getName(), observerWithReception.get().getString(OBSERVED_TYPE));
        assertEquals(TransactionPhase.BEFORE_COMPLETION.name(), observerWithReception.get().getString(TX_PHASE));

    }

}
