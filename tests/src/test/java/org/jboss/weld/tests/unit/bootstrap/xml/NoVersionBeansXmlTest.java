/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.unit.bootstrap.xml;

import static org.testng.Assert.assertEquals;

import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.testng.annotations.Test;

/**
 * Tests behavior for beans.xml with empty version attribute.
 *
 * Tests are using {@code https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd} schema instead of 4.0 which should be used.
 * This is because at the time of writing the 4.0 schema was not yet published. However, this has no effect on the test.
 */
public class NoVersionBeansXmlTest {

    private final WeldBootstrap bootstrap = new WeldBootstrap();

    private BeansXml getBeansXml(String filename) {
        return bootstrap.parse(NoVersionBeansXmlTest.class.getResource(filename));
    }

    @Test
    public void testNoVersionXml() {
        // file has neither version, nor discovery mode
        BeansXml xml = getBeansXml("noversion.xml");
        assertEquals(xml.getBeanDiscoveryMode(), BeanDiscoveryMode.ANNOTATED);
        assertEquals(xml.getVersion(), null);
    }

    @Test
    public void testNoVersionButWithDiscoveryModeXml() {
        // file has no version but has discovery mode
        BeansXml xml = getBeansXml("noversionwithmode.xml");
        assertEquals(xml.getBeanDiscoveryMode(), BeanDiscoveryMode.NONE);
        assertEquals(xml.getVersion(), null);
    }
}
