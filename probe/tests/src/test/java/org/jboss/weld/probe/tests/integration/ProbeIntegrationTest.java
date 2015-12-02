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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.gargoylesoftware.htmlunit.WebClient;

public class ProbeIntegrationTest {

    protected void assertBeanClassVisibleInProbe(Class<?> clazz, List<String> classes) {
        assertTrue("Cannot find class " + clazz.getName() + " in Probe beans list.", classes.contains(clazz.getName()));
    }

    protected WebClient invokeSimpleAction(URL url) throws IOException {
        WebClient client = new WebClient();
        client.getPage(url.toString() + "test");
        return client;
    }
}
