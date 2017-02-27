/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.probe.Strings.BEAN_CLASS;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.UNUSED;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.BEANS_PATH_ALL;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getPageAsJSONObject;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.probe.tests.integration.deployment.beans.unused.AlphaBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.unused.CharlieBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.unused.DeltaBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.unused.NamedBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.unused.ObserverDefiningBean;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ProbeUnusedBeansTest extends ProbeIntegrationTest {

    @ArquillianResource
    private URL url;

    private static final String TEST_ARCHIVE_NAME = "probe-unused-beans-test";

    @Deployment(testable = false)
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, TEST_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeUnusedBeansTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ProbeUnusedBeansTest.class.getPackage(), "beans.xml", "beans.xml")
                .addPackage(AlphaBean.class.getPackage());
    }

    @Test
    public void testUnusedBeans() throws IOException {
        JsonObject page = getPageAsJSONObject(BEANS_PATH_ALL, url);
        assertNotNull(page);
        JsonArray beans = page.getJsonArray(DATA);
        assertFalse(beans.isEmpty());
        assertUnusedBean(beans, ObserverDefiningBean.class, false);
        assertUnusedBean(beans, NamedBean.class, false);
        assertUnusedBean(beans, AlphaBean.class, false);
        assertUnusedBean(beans, CharlieBean.class, false);
        assertUnusedBean(beans, DeltaBean.class, true);
    }

    private void assertUnusedBean(JsonArray beans, Class<?> beanClass, boolean shouldBeUnused) {
        for (JsonValue value : beans) {
            if (value.getValueType().equals(ValueType.OBJECT)) {
                JsonObject bean = (JsonObject) value;
                if (beanClass.getName().equals(bean.getString(BEAN_CLASS))) {
                    if (shouldBeUnused) {
                        assertTrue(beanClass.getName() + " is not unused", bean.containsKey(UNUSED) ? bean.getBoolean(UNUSED) : false);
                    } else {
                        assertFalse(beanClass.getName() + " is unused", bean.containsKey(UNUSED) ? bean.getBoolean(UNUSED) : false);
                    }
                    return;
                }
            }
        }
        fail("Bean not found: " + beanClass);
    }

}
