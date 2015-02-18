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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.jboss.weld.probe.Strings.BEAN_CLASS;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.DEPENDENCIES;
import static org.jboss.weld.probe.Strings.DEPENDENTS;
import static org.jboss.weld.probe.Strings.EJB_NAME;
import static org.jboss.weld.probe.Strings.ENABLEMENT;
import static org.jboss.weld.probe.Strings.ID;
import static org.jboss.weld.probe.Strings.IS_ALTERNATIVE;
import static org.jboss.weld.probe.Strings.KIND;
import static org.jboss.weld.probe.Strings.PRIORITY;
import static org.jboss.weld.probe.Strings.PRIORITY_RANGE;
import static org.jboss.weld.probe.Strings.QUALIFIERS;
import static org.jboss.weld.probe.Strings.SCOPE;
import static org.jboss.weld.probe.Strings.SESSION_BEAN_TYPE;
import static org.jboss.weld.probe.Strings.STEREOTYPES;
import static org.jboss.weld.probe.Strings.TYPES;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.BEANS_PATH;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.BEANS_PATH_ALL;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.BeanType;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.SessionBeanType;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.getAllJsonObjectsByClass;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.getBeanDetail;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.getPageAsJSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import javax.decorator.Decorator;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.probe.integration.tests.annotations.Collector;
import org.jboss.weld.probe.integration.tests.beans.DecoratedInterface;
import org.jboss.weld.probe.integration.tests.beans.ModelBean;
import org.jboss.weld.probe.integration.tests.beans.SessionScopedBean;
import org.jboss.weld.probe.integration.tests.beans.StatefulEjbSession;
import org.jboss.weld.probe.integration.tests.beans.TestDecorator;
import org.jboss.weld.probe.integration.tests.beans.TestProducer;
import org.jboss.weld.probe.integration.tests.interceptors.TestInterceptor;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
public class ProbeBeansTest extends ProbeIntegrationTest {

    @ArquillianResource
    private URL url;

    private static final String TEST_ARCHIVE_NAME = "probe-beans-test";

    @Deployment(testable = false)
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, TEST_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeBeansTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ProbeBeansTest.class.getPackage(), "beans.xml", "beans.xml")
                .addPackage(ProbeBeansTest.class.getPackage())
                .addPackage(TestInterceptor.class.getPackage())
                .addPackage(ModelBean.class.getPackage())
                .addPackage(Collector.class.getPackage());
    }

    @Test
    public void testBeansEndpoint() throws IOException {
        JsonObject beansInTestArchive = getPageAsJSONObject(BEANS_PATH_ALL, url);
        assertNotNull(beansInTestArchive);
        JsonArray beansArray = beansInTestArchive.getJsonArray(DATA);

        assertBeanClassVisibleInProbe(ModelBean.class, beansArray);
        assertBeanClassVisibleInProbe(SessionScopedBean.class, beansArray);
        assertBeanClassVisibleInProbe(TestInterceptor.class, beansArray);
        assertBeanClassVisibleInProbe(StatefulEjbSession.class, beansArray);
        assertBeanClassVisibleInProbe(TestDecorator.class, beansArray);
    }

    @Test
    public void testBeanDetail() throws IOException {
        JsonObject modelBeanDetail = getBeanDetail(BEANS_PATH_ALL, ModelBean.class, url);
        assertEquals(ModelBean.class.getName(), modelBeanDetail.getString(BEAN_CLASS));
        assertEquals(BeanType.MANAGED.name(), modelBeanDetail.getString(KIND));
        assertEquals("@" + RequestScoped.class.getSimpleName(), modelBeanDetail.getString(SCOPE));

        // check @Model bean
        JsonArray qualifiers = modelBeanDetail.getJsonArray(QUALIFIERS);
        JsonArray types = modelBeanDetail.getJsonArray(TYPES);
        JsonArray dependencies = modelBeanDetail.getJsonArray(DEPENDENCIES);
        assertTrue(checkStringInArrayRecursively(Default.class.getSimpleName(), null, qualifiers, false));
        assertTrue(checkStringInArrayRecursively(ModelBean.class.getName(), null, types, false));
        assertTrue(checkStringInArrayRecursively(SessionScopedBean.class.getName(), BEAN_CLASS, dependencies, false));

        // check sessionscoped bean
        JsonObject sessionScopedBeanDetail = getBeanDetail(BEANS_PATH_ALL, SessionScopedBean.class, url);
        assertEquals(SessionScopedBean.class.getName(), sessionScopedBeanDetail.getString(BEAN_CLASS));
        assertEquals(BeanType.MANAGED.name(), sessionScopedBeanDetail.getString(KIND));
        assertEquals("@" + SessionScoped.class.getSimpleName(), sessionScopedBeanDetail.getString(SCOPE));

        types = sessionScopedBeanDetail.getJsonArray(TYPES);
        JsonArray dependents = sessionScopedBeanDetail.getJsonArray(DEPENDENTS);
        assertTrue(checkStringInArrayRecursively(SessionScopedBean.class.getName(), null, types, false));
        assertTrue(checkStringInArrayRecursively(Serializable.class.getName(), null, types, false));
        assertTrue(checkStringInArrayRecursively(ModelBean.class.getName(), BEAN_CLASS, dependents, false));

        // check interceptor detail
        JsonObject testInterceptorDetail = getBeanDetail(BEANS_PATH_ALL, TestInterceptor.class, url);
        assertEquals(TestInterceptor.class.getName(), testInterceptorDetail.getString(BEAN_CLASS));
        assertEquals(BeanType.INTERCEPTOR.name(), testInterceptorDetail.getString(KIND));
        assertEquals("@" + Dependent.class.getSimpleName(), testInterceptorDetail.getString(SCOPE));

    }

    @Test
    public void testProducers() throws IOException {
        JsonObject allBeans = getPageAsJSONObject(BEANS_PATH_ALL, url);
        JsonArray beansData = allBeans.getJsonArray(DATA);

        List<JsonObject> producers = getAllJsonObjectsByClass(TestProducer.class, beansData);

        Optional<JsonObject> fieldProducer = producers.stream().filter((JsonObject o) -> o.getString(KIND).equals(BeanType.PRODUCER_FIELD.name()))
                .findAny();
        assertTrue("Cannot find producer field from " + TestProducer.class.getName(), fieldProducer.isPresent());
        assertEquals("@" + Dependent.class.getSimpleName(), fieldProducer.get().getString(SCOPE));
        assertTrue(checkStringInArrayRecursively(String.class.getName(), null, fieldProducer.get().getJsonArray(TYPES), false));

        Optional<JsonObject> methodProducer = producers.stream().filter((JsonObject o) -> o.getString(KIND).equals(BeanType.PRODUCER_METHOD.name()))
                .findAny();
        assertTrue("Cannot find producer method from " + TestProducer.class.getName(), methodProducer.isPresent());
        assertEquals("@" + Dependent.class.getSimpleName(), fieldProducer.get().getString(SCOPE));
        assertTrue(checkStringInArrayRecursively(ModelBean.class.getName(), null, methodProducer.get().getJsonArray(TYPES), false));
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

        assertEquals(BeanType.SESSION.name(), sessionBeansDetail.getString(KIND));
        assertTrue(checkStringInArrayRecursively(DecoratedInterface.class.getName(), TYPES, sessionBeansDetail.getJsonArray(TYPES), false));
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
        assertTrue(checkStringInArrayRecursively(DecoratedInterface.class.getName(), TYPES, decoratorDetail.getJsonArray(TYPES), false));
        assertTrue(checkStringInArrayRecursively(Serializable.class.getName(), TYPES, decoratorDetail.getJsonArray(TYPES), false));
        assertTrue(checkStringInArrayRecursively(Decorator.class.getName(), STEREOTYPES, decoratorDetail.getJsonArray(STEREOTYPES), false));

    }

}
