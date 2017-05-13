/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Scanning;
import org.jboss.weld.xml.BeansXmlParser;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 *
 */
public class BeansXmlParserTest {

    @Test
    public void testMarker() throws MalformedURLException {
        BeansXmlParser parser = new BeansXmlParser();
        BeansXml beansXml = parser.parse(getBeansXmlURL(""));
        assertNull(beansXml.getVersion());
        assertEquals(BeanDiscoveryMode.ALL, beansXml.getBeanDiscoveryMode());
        assertTrue(beansXml.getEnabledAlternativeClasses().isEmpty());
        assertTrue(beansXml.getEnabledAlternativeStereotypes().isEmpty());
        assertTrue(beansXml.getEnabledDecorators().isEmpty());
        assertTrue(beansXml.getEnabledInterceptors().isEmpty());
        assertEquals(Scanning.EMPTY_SCANNING, beansXml.getScanning());
    }

    @Test
    public void testBeansXml10() throws MalformedURLException {
        BeansXmlParser parser = new BeansXmlParser();
        BeansXml beansXml = parser.parse(getBeansXmlURL("<beans><alternatives><class>com.acme.myfwk.InMemoryDatabase</class></alternatives></beans>"));
        assertNull(beansXml.getVersion());
        assertEquals(BeanDiscoveryMode.ALL, beansXml.getBeanDiscoveryMode());
        assertEquals(1, beansXml.getEnabledAlternativeClasses().size());
        assertEquals("com.acme.myfwk.InMemoryDatabase", beansXml.getEnabledAlternativeClasses().get(0).getValue());
        assertTrue(beansXml.getEnabledAlternativeStereotypes().isEmpty());
        assertTrue(beansXml.getEnabledDecorators().isEmpty());
        assertTrue(beansXml.getEnabledInterceptors().isEmpty());
        assertNotNull(beansXml.getScanning());
        assertTrue(beansXml.getScanning().getExcludes().isEmpty());
        assertTrue(beansXml.getScanning().getIncludes().isEmpty());
    }

    @Test
    public void testBeansXml11() throws MalformedURLException {
        BeansXmlParser parser = new BeansXmlParser();
        BeansXml beansXml = parser.parse(getBeansXmlURL("<beans version=\"1.1\" bean-discovery-mode=\"NONE\"></beans>"));
        assertEquals("1.1", beansXml.getVersion());
        assertEquals(BeanDiscoveryMode.NONE, beansXml.getBeanDiscoveryMode());
        assertTrue(beansXml.getEnabledAlternativeClasses().isEmpty());
        assertTrue(beansXml.getEnabledAlternativeStereotypes().isEmpty());
        assertTrue(beansXml.getEnabledDecorators().isEmpty());
        assertTrue(beansXml.getEnabledInterceptors().isEmpty());
    }

    @Test
    public void testBeansXml11NoDiscoveryMode() throws MalformedURLException {
        BeansXmlParser parser = new BeansXmlParser();
        BeansXml beansXml = parser.parse(getBeansXmlURL("<beans version=\"1.1\"></beans>"));
        assertEquals("1.1", beansXml.getVersion());
        assertEquals(BeanDiscoveryMode.ANNOTATED, beansXml.getBeanDiscoveryMode());
        assertTrue(beansXml.getEnabledAlternativeClasses().isEmpty());
        assertTrue(beansXml.getEnabledAlternativeStereotypes().isEmpty());
        assertTrue(beansXml.getEnabledDecorators().isEmpty());
        assertTrue(beansXml.getEnabledInterceptors().isEmpty());
        assertNotNull(beansXml.getScanning());
        assertTrue(beansXml.getScanning().getExcludes().isEmpty());
        assertTrue(beansXml.getScanning().getIncludes().isEmpty());
    }

    private URL getBeansXmlURL(final String content) throws MalformedURLException {
        return new URL(null, "test:", new URLStreamHandler() {

            @Override
            protected URLConnection openConnection(URL url) throws IOException {
                return new URLConnection(url) {

                    @Override
                    public void connect() throws IOException {
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                    }

                };
            }
        });
    }

}
