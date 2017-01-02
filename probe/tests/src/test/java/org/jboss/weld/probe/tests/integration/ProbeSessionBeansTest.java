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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.jboss.weld.probe.Strings.CLASS;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.EJB_NAME;
import static org.jboss.weld.probe.Strings.ENABLEMENT;
import static org.jboss.weld.probe.Strings.ID;
import static org.jboss.weld.probe.Strings.IS_ALTERNATIVE;
import static org.jboss.weld.probe.Strings.KIND;
import static org.jboss.weld.probe.Strings.PRIORITY;
import static org.jboss.weld.probe.Strings.PRIORITY_RANGE;
import static org.jboss.weld.probe.Strings.SESSION_BEAN_TYPE;
import static org.jboss.weld.probe.Strings.STEREOTYPES;
import static org.jboss.weld.probe.Strings.TYPES;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.BEANS_PATH;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.BEANS_PATH_ALL;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getAllJsonObjectsByClass;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getPageAsJSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

import javax.decorator.Decorator;
import javax.json.JsonArray;
import javax.json.JsonObject;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.probe.tests.integration.JSONTestUtil.BeanType;
import org.jboss.weld.probe.tests.integration.JSONTestUtil.SessionBeanType;
import org.jboss.weld.probe.tests.integration.deployment.InvokingServlet;
import org.jboss.weld.probe.tests.integration.deployment.annotations.Collector;
import org.jboss.weld.probe.tests.integration.deployment.beans.ModelBean;
import org.jboss.weld.probe.tests.integration.deployment.interceptors.TestInterceptor;
import org.jboss.weld.probe.tests.integration.deployment.sessions.DecoratedInterface;
import org.jboss.weld.probe.tests.integration.deployment.sessions.StatefulEjbSession;
import org.jboss.weld.probe.tests.integration.deployment.sessions.TestDecorator;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
public class ProbeSessionBeansTest extends ProbeIntegrationTest {

    @ArquillianResource
    private URL url;

    private static final String TEST_ARCHIVE_NAME = "probe-session-beans-test";

    @Deployment(testable = false)
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, TEST_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeSessionBeansTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ProbeSessionBeansTest.class.getPackage(), "beans.xml", "beans.xml")
                .addClass(InvokingServlet.class)
                .addPackage(TestInterceptor.class.getPackage())
                .addPackage(ModelBean.class.getPackage())
                .addPackage(Collector.class.getPackage())
                .addPackage(DecoratedInterface.class.getPackage());
    }

    @Test
    public void testEjbSessionBeans() throws IOException {
        JsonObject allBeans = getPageAsJSONObject(BEANS_PATH_ALL, url);
        JsonArray beansData = allBeans.getJsonArray(DATA);

        List<JsonObject> statefulEjbSessionList = getAllJsonObjectsByClass(StatefulEjbSession.class, beansData);
        assertEquals(statefulEjbSessionList.size(), 1);
        JsonObject statefulEjbJson = statefulEjbSessionList.get(0);
        String statefulEjbSessionId = statefulEjbJson.getString(ID);
        JsonObject sessionBeansDetail = getPageAsJSONObject(BEANS_PATH + "/" + statefulEjbSessionId, url);

        ReadContext ctx = JsonPath.parse(sessionBeansDetail.toString());
        List<String> types = ctx.read("$." + TYPES, List.class);

        assertEquals(BeanType.SESSION.name(), sessionBeansDetail.getString(KIND));
        assertTrue(types.contains(DecoratedInterface.class.getName()));
        assertEquals(Boolean.TRUE.booleanValue(), sessionBeansDetail.getBoolean(IS_ALTERNATIVE));
        assertEquals(Boolean.TRUE.booleanValue(), sessionBeansDetail.getBoolean(EJB_NAME));
        assertEquals(SessionBeanType.STATEFUL.name(), sessionBeansDetail.getString(SESSION_BEAN_TYPE));

        JsonObject sessionBeanEnablement = sessionBeansDetail.getJsonObject(ENABLEMENT);
        // TODO introduce enum with priority ranges
        assertEquals("APPLICATION", sessionBeanEnablement.getString(PRIORITY_RANGE));
        assertEquals(2500, sessionBeanEnablement.getInt(PRIORITY));
    }

    @Test
    public void testDecorator() throws IOException {
        JsonObject allBeans = getPageAsJSONObject(BEANS_PATH_ALL, url);
        JsonArray beansData = allBeans.getJsonArray(DATA);

        List<JsonObject> testDecorators = getAllJsonObjectsByClass(TestDecorator.class, beansData);
        JsonObject testDecoratorJson = testDecorators.get(0);
        assertNotNull("Cannot find any " + TestDecorator.class.getName(), testDecoratorJson);

        String decoratorId = testDecoratorJson.getString(ID);
        JsonObject decoratorDetail = getPageAsJSONObject(BEANS_PATH + "/" + decoratorId, url);

        assertEquals(BeanType.DECORATOR.name(), decoratorDetail.getString(KIND));
        ReadContext ctx = JsonPath.parse(decoratorDetail.toString());
        List<String> types = ctx.read("$." + TYPES, List.class);
        List<String> stereotypes = ctx.read("$." + STEREOTYPES + "[*]." + CLASS, List.class);

        assertTrue(types.contains(DecoratedInterface.class.getName()));
        assertTrue(types.contains(Serializable.class.getName()));
        assertTrue(stereotypes.contains(Decorator.class.getName()));

    }

}
