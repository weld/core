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
import static org.jboss.weld.probe.Strings.BEAN_CLASS;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.DEPENDENCIES;
import static org.jboss.weld.probe.Strings.DEPENDENTS;
import static org.jboss.weld.probe.Strings.KIND;
import static org.jboss.weld.probe.Strings.QUALIFIERS;
import static org.jboss.weld.probe.Strings.SCOPE;
import static org.jboss.weld.probe.Strings.TYPES;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.BEANS_PATH_ALL;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.BeanType;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getAllJsonObjectsByClass;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getBeanDetail;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getBeanInstanceDetail;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getPageAsJSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
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
import org.jboss.weld.probe.tests.integration.deployment.InvokingServlet;
import org.jboss.weld.probe.tests.integration.deployment.annotations.Collector;
import org.jboss.weld.probe.tests.integration.deployment.beans.ApplicationScopedObserver;
import org.jboss.weld.probe.tests.integration.deployment.beans.ConversationBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.ModelBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.SessionScopedBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.TestProducer;
import org.jboss.weld.probe.tests.integration.deployment.interceptors.TestInterceptor;
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
                .addClass(InvokingServlet.class)
                .addPackage(TestInterceptor.class.getPackage())
                .addPackage(ModelBean.class.getPackage())
                .addPackage(Collector.class.getPackage());
    }

    @Test
    public void testBeansEndpoint() throws IOException {
        JsonObject beansInTestArchive = getPageAsJSONObject(BEANS_PATH_ALL, url);
        assertNotNull(beansInTestArchive);

        ReadContext ctx = JsonPath.parse(beansInTestArchive.toString());
        List<String> beanClasses = ctx.read("$." + DATA + "[*]." + BEAN_CLASS, List.class);
        assertBeanClassVisibleInProbe(ModelBean.class, beanClasses);
        assertBeanClassVisibleInProbe(SessionScopedBean.class, beanClasses);
        assertBeanClassVisibleInProbe(TestInterceptor.class, beanClasses);
    }

    @Test
    public void testBeanDetail() throws IOException {
        JsonObject modelBeanDetail = getBeanDetail(BEANS_PATH_ALL, ModelBean.class, url);
        assertEquals(ModelBean.class.getName(), modelBeanDetail.getString(BEAN_CLASS));
        assertEquals(BeanType.MANAGED.name(), modelBeanDetail.getString(KIND));
        assertEquals("@" + RequestScoped.class.getSimpleName(), modelBeanDetail.getString(SCOPE));

        // check @Model bean
        ReadContext ctx = JsonPath.parse(modelBeanDetail.toString());
        List<String> qualifiers = ctx.read("$." + QUALIFIERS + "[*]", List.class);
        List<String> types = ctx.read("$." + TYPES + "[*]", List.class);
        List<String> dependencies = ctx.read("$." + DEPENDENCIES + "[*]." + BEAN_CLASS, List.class);

        assertTrue(qualifiers.contains("@" + Default.class.getSimpleName()));
        assertBeanClassVisibleInProbe(ModelBean.class, types);
        assertBeanClassVisibleInProbe(SessionScopedBean.class, dependencies);

        // check sessionscoped bean
        JsonObject sessionScopedBeanDetail = getBeanDetail(BEANS_PATH_ALL, SessionScopedBean.class, url);
        assertEquals(SessionScopedBean.class.getName(), sessionScopedBeanDetail.getString(BEAN_CLASS));
        assertEquals(BeanType.MANAGED.name(), sessionScopedBeanDetail.getString(KIND));
        assertEquals("@" + SessionScoped.class.getSimpleName(), sessionScopedBeanDetail.getString(SCOPE));

        //types = sessionScopedBeanDetail.getJsonArray(TYPES);
        ctx = JsonPath.parse(sessionScopedBeanDetail.toString());
        types = ctx.read("$." + TYPES + "[*]", List.class);
        List<String> dependents = ctx.read("$." + DEPENDENTS + "[*].beanClass", List.class);
        assertBeanClassVisibleInProbe(SessionScopedBean.class, types);
        assertBeanClassVisibleInProbe(Serializable.class, types);
        assertBeanClassVisibleInProbe(ModelBean.class, dependents);

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

        ReadContext ctx = JsonPath.parse(fieldProducer.get().toString());
        List<String> types = ctx.read("$." + TYPES + "[*]", List.class);
        assertTrue(types.contains(String.class.getName()));

        Optional<JsonObject> methodProducer = producers.stream().filter((JsonObject o) -> o.getString(KIND).equals(BeanType.PRODUCER_METHOD.name()))
                .findAny();
        assertTrue("Cannot find producer method from " + TestProducer.class.getName(), methodProducer.isPresent());
        assertEquals("@" + Dependent.class.getSimpleName(), methodProducer.get().getString(SCOPE));
        ctx = JsonPath.parse(methodProducer.get().toString());
        types = ctx.read("$." + TYPES + "[*]", List.class);
        assertTrue(types.contains(ModelBean.class.getName()));
    }

    @Test
    public void testBeanInstanceDetail() throws IOException {
        WebClient webClient = invokeSimpleAction(url);

        // sessionscoped bean instance 
        JsonObject sessionBeanInstance = getBeanInstanceDetail(BEANS_PATH_ALL, SessionScopedBean.class, url, webClient);
        assertEquals(BeanType.MANAGED.name(), sessionBeanInstance.getString(KIND));
        assertEquals(SessionScopedBean.class.getName(), sessionBeanInstance.getString(BEAN_CLASS));
        assertEquals("@" + SessionScoped.class.getSimpleName(), sessionBeanInstance.getString(SCOPE));

        // applicationscoped bean instance 
        JsonObject applicationScopedBeanInstance = getBeanInstanceDetail(BEANS_PATH_ALL, ApplicationScopedObserver.class, url, webClient);
        assertEquals(BeanType.MANAGED.name(), applicationScopedBeanInstance.getString(KIND));
        assertEquals(ApplicationScopedObserver.class.getName(), applicationScopedBeanInstance.getString(BEAN_CLASS));
        assertEquals("@" + ApplicationScoped.class.getSimpleName(), applicationScopedBeanInstance.getString(SCOPE));

        // conversationscoped bean instance
        JsonObject conversationScopedBeanInstance = getBeanInstanceDetail(BEANS_PATH_ALL, ConversationBean.class, url, webClient, "?cid=1");
        assertEquals(BeanType.MANAGED.name(), conversationScopedBeanInstance.getString(KIND));
        assertEquals(ConversationBean.class.getName(), conversationScopedBeanInstance.getString(BEAN_CLASS));
        assertEquals("@" + ConversationScoped.class.getSimpleName(), conversationScopedBeanInstance.getString(SCOPE));
    }

}
