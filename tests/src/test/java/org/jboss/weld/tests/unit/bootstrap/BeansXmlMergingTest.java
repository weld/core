/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.unit.bootstrap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.BeansXmlRecord;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.testng.annotations.Test;

/**
 *
 */
public class BeansXmlMergingTest {

    @Test
    public void testDuplicatesAreRemoved() {
        WeldBootstrap weldBootstrap = new WeldBootstrap();

        URL url1 = getClass().getResource("/org/jboss/weld/tests/unit/bootstrap/xml/beans1.xml");
        URL url2 = getClass().getResource("/org/jboss/weld/tests/unit/bootstrap/xml/beans2.xml");

        BeansXml beansXml = weldBootstrap.parse(Arrays.asList(url1, url1, url2), true);

        assertEquals(2, beansXml.getEnabledAlternatives().size());
        Set<String> alternativeClassNames = new HashSet<String>();
        for (Metadata<BeansXmlRecord> record : beansXml.getEnabledAlternatives()) {
            alternativeClassNames.add(record.getValue().getValue());
        }
        assertTrue(alternativeClassNames.contains("org.jboss.weld.tests.unit.bootstrap.xml.Stereo"));
        assertTrue(alternativeClassNames.contains("org.jboss.weld.tests.unit.bootstrap.xml.Alt"));

        assertEquals(1, beansXml.getEnabledInterceptors().size());
        assertEquals("org.jboss.weld.tests.unit.bootstrap.xml.Int", beansXml.getEnabledInterceptors().get(0).getValue().getValue());

        assertEquals(1, beansXml.getEnabledDecorators().size());
        assertEquals("org.jboss.weld.tests.unit.bootstrap.xml.Dec", beansXml.getEnabledDecorators().get(0).getValue().getValue());
    }

    @Test
    public void testDuplicatesInSingleFileAreNotRemoved() {
        WeldBootstrap weldBootstrap = new WeldBootstrap();

        URL url = getClass().getResource("/org/jboss/weld/tests/unit/bootstrap/xml/beans3.xml");
        BeansXml beansXml = weldBootstrap.parse(Arrays.asList(url), true);

        assertEquals(4, beansXml.getEnabledAlternatives().size());
        assertEquals(2, beansXml.getEnabledInterceptors().size());
        assertEquals(2, beansXml.getEnabledDecorators().size());
    }
}
