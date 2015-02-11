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
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.*;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.probe.integration.tests.annotations.Collector;
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
                .addPackage(Collector.class.getPackage());
    }

    @Test
    public void testBeansEndpoint() throws IOException {
        JsonObject beansInTestArchive = getPageAsJSONObject(BEANS_PATH_ALL, url);
        assertNotNull(beansInTestArchive);
        JsonArray beansArray = beansInTestArchive.getAsJsonArray(DATA);

        assertBeanClassVisibleInProbe(ModelBean.class, beansArray);
        assertBeanClassVisibleInProbe(SessionScopedBean.class, beansArray);
        assertBeanClassVisibleInProbe(TestInterceptor.class, beansArray);
    }

    @Test
    public void testBeanDetail() throws IOException {
        JsonObject modelBeanDetail = getBeanDetail(BEANS_PATH_ALL, ModelBean.class, url);
        assertEquals(ModelBean.class.getName(), modelBeanDetail.get(BEAN_CLASS).getAsString());
        assertEquals(BeanType.MANAGED.name(), modelBeanDetail.get(KIND).getAsString());
        assertEquals("@" + RequestScoped.class.getSimpleName(), modelBeanDetail.get(SCOPE).getAsString());

        // check @Model bean
        JsonArray qualifiers = modelBeanDetail.getAsJsonArray(QUALIFIERS);
        JsonArray types = modelBeanDetail.getAsJsonArray(TYPES);
        JsonArray dependencies = modelBeanDetail.getAsJsonArray(DEPENDENCIES);
        assertTrue(checkStringInArrayRecursively(Default.class.getSimpleName(), null, qualifiers, false));
        assertTrue(checkStringInArrayRecursively(ModelBean.class.getName(), null, types, false));
        assertTrue(checkStringInArrayRecursively(SessionScopedBean.class.getName(), BEAN_CLASS, dependencies, false));

        // check sessionscoped bean
        JsonObject sessionScopedBeanDetail = getBeanDetail(BEANS_PATH_ALL, SessionScopedBean.class, url);
        assertEquals(SessionScopedBean.class.getName(), sessionScopedBeanDetail.get(BEAN_CLASS).getAsString());
        assertEquals(BeanType.MANAGED.name(), sessionScopedBeanDetail.get(KIND).getAsString());
        assertEquals("@" + SessionScoped.class.getSimpleName(), sessionScopedBeanDetail.get(SCOPE).getAsString());

        types = sessionScopedBeanDetail.getAsJsonArray(TYPES);
        JsonArray dependents = sessionScopedBeanDetail.getAsJsonArray(DEPENDENTS);
        assertTrue(checkStringInArrayRecursively(SessionScopedBean.class.getName(), null, types, false));
        assertTrue(checkStringInArrayRecursively(Serializable.class.getName(), null, types, false));
        assertTrue(checkStringInArrayRecursively(ModelBean.class.getName(), BEAN_CLASS, dependents, false));

        // check interceptor detail
        JsonObject testInterceptorDetail = getBeanDetail(BEANS_PATH_ALL, TestInterceptor.class, url);
        assertEquals(TestInterceptor.class.getName(), testInterceptorDetail.get(BEAN_CLASS).getAsString());
        assertEquals(BeanType.INTERCEPTOR.name(), testInterceptorDetail.get(KIND).getAsString());
        assertEquals("@" + Dependent.class.getSimpleName(), testInterceptorDetail.get(SCOPE).getAsString());

    }

}
