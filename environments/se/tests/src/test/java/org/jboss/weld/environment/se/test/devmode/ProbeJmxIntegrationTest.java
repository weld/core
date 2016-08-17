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
package org.jboss.weld.environment.se.test.devmode;

import static org.jboss.weld.environment.se.test.util.ProbeJMXUtil.invokeMBeanOperation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.enterprise.context.Dependent;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.test.util.ProbeJMXUtil;
import org.jboss.weld.probe.ProbeExtension;
import org.jboss.weld.probe.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ProbeJmxIntegrationTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class).addClasses(ProbeJmxIntegrationTest.class, Omega.class, OmegaObserver.class, ProbeJMXUtil.class))
                .addSystemProperty(Weld.DEV_MODE_SYSTEM_PROPERTY, "true").addSystemProperty(ConfigurationKey.PROBE_JMX_SUPPORT.get(), "true").build();
    }

    @Test
    public void testReceiveBeans() throws Exception {
        try (WeldContainer container = new Weld().initialize()) {
            assertNotNull(container.select(ProbeExtension.class).get());
            container.select(Omega.class).get().ping();
            Object[] params = new Object[] { 0, 50, "", "" };
            String[] signature = new String[] { int.class.getName(), int.class.getName(), String.class.getName(), String.class.getName() };
            JsonObject obj = invokeMBeanOperation("receiveBeans", params, signature);
            JsonArray beansDataArray = obj.getJsonArray(Strings.DATA);
            JsonObject omegaBeanJson = getJsonObjectByClass(beansDataArray, Omega.class);
            assertNotNull(omegaBeanJson);
            assertEquals(omegaBeanJson.getJsonString(Strings.SCOPE).getString(), "@" + Dependent.class.getSimpleName());
        }
    }

    @Test
    public void testReceiveObservers() throws Exception {
        try (WeldContainer container = new Weld().initialize()) {
            assertNotNull(container.select(ProbeExtension.class).get());
            Object[] params = new Object[] { 0, 50, "", "" };
            String[] signature = new String[] { int.class.getName(), int.class.getName(), String.class.getName(), String.class.getName() };
            JsonObject obj = invokeMBeanOperation("receiveObservers", params, signature);
            JsonArray beansDataArray = obj.getJsonArray(Strings.DATA);
            JsonObject omegaObserverJson = getJsonObjectByClass(beansDataArray, OmegaObserver.class);
            assertNotNull(omegaObserverJson);
            assertEquals(omegaObserverJson.getJsonString(Strings.OBSERVED_TYPE).getString(), Omega.class.getName());
            assertEquals(omegaObserverJson.getJsonString(Strings.RECEPTION).getString(), "ALWAYS");
            assertEquals(omegaObserverJson.getJsonString(Strings.PRIORITY_RANGE).getString(), Strings.APPLICATION.toUpperCase());
            assertEquals(omegaObserverJson.getInt(Strings.PRIORITY), javax.interceptor.Interceptor.Priority.APPLICATION + 500);
        }
    }

    private JsonObject getJsonObjectByClass(JsonArray array, Class<?> clazz) {
        for (int i = 0; i < array.size(); i++) {
            if (array.getJsonObject(i).getJsonString(Strings.BEAN_CLASS).getString().equals(clazz.getName())) {
                return array.getJsonObject(i);
            }
        }
        return null;
    }

}
