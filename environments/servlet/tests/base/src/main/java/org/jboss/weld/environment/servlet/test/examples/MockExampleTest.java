/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.servlet.test.examples;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MockExampleTest {

    @Deployment
    public static WebArchive createTestArchive() {
        // BeanDiscoveryMode.ALL because many tests have 0 beans to discover and Weld would just skip initialization
        return baseDeployment(new BeansXml(BeanDiscoveryMode.ALL).alternatives(MockSentenceTranslator.class)).addPackage(MockExampleTest.class.getPackage());
    }

    @Test
    public void testMockSentenceTranslator(TextTranslator textTranslator) throws Exception {
        assertEquals("Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.", textTranslator.translate("Hello world. How's tricks?"));
    }

}
